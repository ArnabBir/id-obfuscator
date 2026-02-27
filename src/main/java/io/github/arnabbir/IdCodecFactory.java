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
     * Creates an {@link IdCodec} based on modular multiplication.
     * <p>
     * Requirements:
     * <ul>
     *   <li>{@code modulus > 1}</li>
     *   <li>{@code gcd(multiplier, modulus) == 1}</li>
     * </ul>
     *
     * @param multiplier the multiplier 'a'
     * @param modulus    the modulus 'm'
     * @return a thread-safe, immutable codec
     * @throws IllegalArgumentException if parameters are invalid
     */
    public static IdCodec create(long multiplier, long modulus) {
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
        return new EeaIdCodec(multiplier, inverse, modulus);
    }
}
