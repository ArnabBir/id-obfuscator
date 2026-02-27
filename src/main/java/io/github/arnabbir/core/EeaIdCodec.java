package io.github.arnabbir.core;

import java.io.Serial;
import java.io.Serializable;

import io.github.arnabbir.IdCodec;

/**
 * An {@link IdCodec} based on modular multiplication and the Extended Euclidean Algorithm (EEA).
 * <p>
 * Encoding:  {@code (id * multiplier) mod modulus}
 * <br>
 * Decoding:  {@code (encoded * inverseMultiplier) mod modulus}
 * <p>
 * This class is immutable and thread safe.
 */
public final class EeaIdCodec implements IdCodec, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final long multiplier; // normalized to [0, modulus)
    private final long inverseMultiplier; // normalized to [0, modulus)
    private final long modulus;

    public EeaIdCodec(long multiplier, long inverseMultiplier, long modulus) {
        if (modulus <= 1) {
            throw new IllegalArgumentException("modulus must be > 1");
        }
        this.modulus = modulus;
        this.multiplier = Math.floorMod(multiplier, modulus);
        this.inverseMultiplier = Math.floorMod(inverseMultiplier, modulus);
    }

    @Override
    public long encode(long id) {
        return modularMultiply(Math.floorMod(id, modulus), multiplier, modulus);
    }

    @Override
    public long decode(long id) {
        return modularMultiply(Math.floorMod(id, modulus), inverseMultiplier, modulus);
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

    @Override
    public String toString() {
        return "EeaIdCodec{" +
                "multiplier=" + multiplier +
                ", inverseMultiplier=" + inverseMultiplier +
                ", modulus=" + modulus +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EeaIdCodec that)) return false;
        return multiplier == that.multiplier
                && inverseMultiplier == that.inverseMultiplier
                && modulus == that.modulus;
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(multiplier);
        result = 31 * result + Long.hashCode(inverseMultiplier);
        result = 31 * result + Long.hashCode(modulus);
        return result;
    }
}
