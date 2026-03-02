package io.github.arnabbir;

import io.github.arnabbir.core.EeaIdCodec;
import io.github.arnabbir.math.ModularArithmetic;

/**
 * Factory for creating {@link IdCodec} instances.
 */
public final class IdCodecFactory {

    private IdCodecFactory() {
        // static factory
    }

    /**
     * Creates an {@link IdCodec} based on modular multiplication with default version and version bits.
     * <p>
     * Requirements:
     * <ul>
     *   <li>{@code modulus > 1}</li>
     *   <li>{@code gcd(multiplier, modulus) == 1}</li>
     * </ul>
     *
     * @param multiplier the multiplier 'a'
     * @param modulus    the modulus 'm'
     * @return a thread-safe, immutable codec with default version and version bits
     * @throws IllegalArgumentException if parameters are invalid
     */
    public static IdCodec create(long multiplier, long modulus) {
        return create(multiplier, modulus, EeaIdCodec.DEFAULT_VERSION, EeaIdCodec.DEFAULT_VERSION_BITS);
    }

    /**
     * Creates an {@link IdCodec} based on modular multiplication with a specified version and default version bits.
     * <p>
     * Versioning enables algorithm changes in the future without invalidating existing encoded IDs.
     * <p>
     * Requirements:
     * <ul>
     *   <li>{@code modulus > 1}</li>
     *   <li>{@code gcd(multiplier, modulus) == 1}</li>
     *   <li>{@code version} in valid range for the default version bits</li>
     * </ul>
     *
     * @param multiplier the multiplier 'a'
     * @param modulus    the modulus 'm'
     * @param version    the codec version; enables future algorithm changes
     * @return a thread-safe, immutable codec with specified version
     * @throws IllegalArgumentException if parameters are invalid
     */
    public static IdCodec create(long multiplier, long modulus, int version) {
        return create(multiplier, modulus, version, EeaIdCodec.DEFAULT_VERSION_BITS);
    }

    /**
     * Creates an {@link IdCodec} based on modular multiplication with specified version and version bit length.
     * <p>
     * Versioning enables algorithm changes in the future without invalidating existing encoded IDs.
     * The version is stored in the highest versionBits of encoded values.
     * <p>
     * Requirements:
     * <ul>
     *   <li>{@code modulus > 1}</li>
     *   <li>{@code gcd(multiplier, modulus) == 1}</li>
     *   <li>{@code versionBits} in range [1, 63]</li>
     *   <li>{@code version} in range [0, 2^versionBits - 1]</li>
     * </ul>
     *
     * @param multiplier   the multiplier 'a'
     * @param modulus      the modulus 'm'
     * @param version      the codec version; enables future algorithm changes
     * @param versionBits  the number of bits reserved for version encoding
     * @return a thread-safe, immutable codec with specified version and version bits
     * @throws IllegalArgumentException if parameters are invalid
     */
    public static IdCodec create(long multiplier, long modulus, int version, int versionBits) {
        if (modulus <= 1) {
            throw new IllegalArgumentException("modulus must be > 1");
        }

        long gcd = ModularArithmetic.gcd(multiplier, modulus);
        if (gcd != 1L) {
            throw new IllegalArgumentException(
                    "multiplier and modulus must be coprime; gcd=" + gcd);
        }

        long inverse = ModularArithmetic.calculateInverse(multiplier, modulus);
        // Normalize multiplier/inverse inside EeaIdCodec using floorMod.
        return new EeaIdCodec(multiplier, inverse, modulus, version, versionBits);
    }
}
