package io.github.arnabbir.core;

import java.io.Serial;
import java.io.Serializable;

import io.github.arnabbir.IdCodec;

/**
 * An {@link IdCodec} based on modular multiplication and the Extended Euclidean Algorithm (EEA).
 * <p>
 * Encoding:  {@code (id * multiplier) mod modulus}, with version encoded in the highest bits
 * <br>
 * Decoding:  {@code (encoded * inverseMultiplier) mod modulus}
 * <p>
 * Versioning allows for algorithm changes in the future without breaking existing encoded IDs.
 * <p>
 * This class is immutable and thread safe.
 */
public final class EeaIdCodec implements IdCodec, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Default codec version. Version is stored in the highest bits of encoded values,
     * allowing for future algorithm changes.
     */
    public static final int DEFAULT_VERSION = 0;

    /** Default number of bits reserved for version encoding */
    public static final int DEFAULT_VERSION_BITS = 0;

    private final long multiplier; // normalized to [0, modulus)
    private final long inverseMultiplier; // normalized to [0, modulus)
    private final long modulus;
    private final int version;
    private final int versionBits;
    private final long versionMask;
    private final int versionShift;

    /**
     * Creates an EeaIdCodec with the default version and version bit length.
     *
     * @param multiplier the multiplier for encoding
     * @param inverseMultiplier the inverse multiplier for decoding
     * @param modulus the modulus for modular arithmetic
     * @throws IllegalArgumentException if modulus <= 1
     */
    public EeaIdCodec(long multiplier, long inverseMultiplier, long modulus) {
        this(multiplier, inverseMultiplier, modulus, DEFAULT_VERSION, DEFAULT_VERSION_BITS);
    }

    /**
     * Creates an EeaIdCodec with a specified version and default version bit length.
     *
     * @param multiplier the multiplier for encoding
     * @param inverseMultiplier the inverse multiplier for decoding
     * @param modulus the modulus for modular arithmetic
     * @param version the version number; enables future algorithm changes
     * @throws IllegalArgumentException if modulus <= 1 or version not in valid range
     */
    public EeaIdCodec(long multiplier, long inverseMultiplier, long modulus, int version) {
        this(multiplier, inverseMultiplier, modulus, version, DEFAULT_VERSION_BITS);
    }

    /**
     * Creates an EeaIdCodec with a specified version and version bit length.
     *
     * @param multiplier the multiplier for encoding
     * @param inverseMultiplier the inverse multiplier for decoding
     * @param modulus the modulus for modular arithmetic
     * @param version the version number; enables future algorithm changes
     * @param versionBits the number of bits reserved for version encoding (1-63);
     *                    determines the maximum version value: 2^versionBits - 1
     * @throws IllegalArgumentException if parameters are invalid
     */
    public EeaIdCodec(long multiplier, long inverseMultiplier, long modulus, int version, int versionBits) {
        if (modulus <= 1) {
            throw new IllegalArgumentException("modulus must be > 1");
        }
        if (versionBits < 0 || versionBits > 63) {
            throw new IllegalArgumentException("versionBits must be in range [0, 63]");
        }
        long maxVersion = versionBits == 0 ? 0 : (1L << versionBits) - 1;
        if (versionBits > 0 && (version < 0 || version > maxVersion)) {
            throw new IllegalArgumentException(
                    "version must be in range [0, " + maxVersion + "] for versionBits=" + versionBits);
        }
        if (versionBits == 0 && version != 0) {
            throw new IllegalArgumentException(
                    "version must be 0 when versionBits is 0");
        }
        this.modulus = modulus;
        this.multiplier = Math.floorMod(multiplier, modulus);
        this.inverseMultiplier = Math.floorMod(inverseMultiplier, modulus);
        this.version = version;
        this.versionBits = versionBits;
        this.versionShift = versionBits == 0 ? 0 : 64 - versionBits;
        this.versionMask = versionBits == 0 ? 0 : (maxVersion << versionShift);
    }

    @Override
    public long encode(long id) {
        long encoded = modularMultiply(Math.floorMod(id, modulus), multiplier, modulus);
        // Encode version in the highest bits
        return encodeVersion(encoded, version);
    }

    @Override
    public long decode(long id) {
        // Extract and verify version from the highest bits (if version bits are used)
        if (versionBits > 0) {
            int extractedVersion = extractVersion(id);
            if (extractedVersion != version) {
                throw new IllegalArgumentException(
                        "version mismatch: expected " + version + ", got " + extractedVersion);
            }
        }
        // Decode using only the lower bits (without version)
        long stripped = stripVersion(id);
        return modularMultiply(Math.floorMod(stripped, modulus), inverseMultiplier, modulus);
    }

    /**
     * Encodes a value by setting the version in the highest bits.
     */
    private long encodeVersion(long value, int ver) {
        if (versionBits == 0) {
            return value; // No version bits to encode
        }
        // Clear the version bits and set the new version
        return (value & ~versionMask) | ((long) ver << versionShift);
    }

    /**
     * Extracts the version from the highest bits.
     */
    private int extractVersion(long value) {
        if (versionBits == 0) {
            return 0; // No version bits
        }
        return (int) ((value & versionMask) >>> versionShift);
    }

    /**
     * Removes the version bits from the highest bits.
     */
    private long stripVersion(long value) {
        if (versionBits == 0) {
            return value; // No version bits to strip
        }
        return value & ~versionMask;
    }

    /**
     * Computes (a * b) mod m without overflowing signed 64 bit arithmetic.
     * <p>
     * Runs in at most 63 iterations (bounded by the number of bits in a signed long).
     */
    static long modularMultiply(long a, long b, long m) {
        // Preconditions: m > 0; a and b are expected in [0, m)
        long result = 0L;
        long addend = a;
        long multiplier = b;

        while (multiplier != 0L) {
            if ((multiplier & 1L) != 0L) {
                result = addMod(result, addend, m);
            }
            multiplier >>>= 1;
            if (multiplier != 0L) { // avoid one extra addMod at the end
                addend = addMod(addend, addend, m);
            }
        }
        return result;
    }

    /**
     * Computes (x + y) mod m, assuming x,y in [0,m) and m>0, without overflow.
     */
    private static long addMod(long x, long y, long m) {
        // If x + y >= m then x + y - m, else x + y.
        // Implemented without overflow by comparing x to (m - y).
        long threshold = m - y;
        if (x >= threshold) {
            return x - threshold;
        }
        return x + y;
    }

    public long getMultiplier() {
        return multiplier;
    }

    public long getInverseMultiplier() {
        return inverseMultiplier;
    }

    public long getModulus() {
        return modulus;
    }

    public int getVersion() {
        return version;
    }

    public int getVersionBits() {
        return versionBits;
    }

    @Override
    public String toString() {
        return "EeaIdCodec{" +
                "multiplier=" + multiplier +
                ", inverseMultiplier=" + inverseMultiplier +
                ", modulus=" + modulus +
                ", version=" + version +
                ", versionBits=" + versionBits +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EeaIdCodec that)) return false;
        return multiplier == that.multiplier
                && inverseMultiplier == that.inverseMultiplier
                && modulus == that.modulus
                && version == that.version
                && versionBits == that.versionBits;
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(multiplier);
        result = 31 * result + Long.hashCode(inverseMultiplier);
        result = 31 * result + Long.hashCode(modulus);
        result = 31 * result + version;
        result = 31 * result + versionBits;
        return result;
    }
}
