package io.github.arnabbir.math;

import java.math.BigInteger;

/**
 * Utility methods for modular arithmetic.
 */
public final class ModularArithmetic {

    private ModularArithmetic() {
        // utility
    }

    /**
     * Computes {@code gcd(a, b)}.
     * <p>
     * The result is always non-negative.
     * <p>
     * Note: for the edge case {@code gcd(Long.MIN_VALUE, 0)} the mathematical
     * result is {@code 2^63}, which cannot be represented as a signed {@code long}.
     * In such cases this method throws {@link ArithmeticException}.
     */
    public static long gcd(long a, long b) {
        // BigInteger-based gcd is safe for all long values (including Long.MIN_VALUE).
        // We use longValueExact() to avoid silent overflow.
        return BigInteger.valueOf(a).gcd(BigInteger.valueOf(b)).longValueExact();
    }

    /**
     * Calculates the modular multiplicative inverse of {@code a} modulo {@code m}.
     * <p>
     * Returns {@code x} such that {@code (a * x) % m == 1}. If the inverse does not exist,
     * throws {@link ArithmeticException}.
     *
     * @param a the value to invert
     * @param m the modulus (must be &gt; 0)
     */
    public static long calculateInverse(long a, long m) {
        if (m <= 0) {
            throw new IllegalArgumentException("Modulus must be positive");
        }

        // Normalize 'a' into [0, m)
        BigInteger modulus = BigInteger.valueOf(m);
        BigInteger newR = BigInteger.valueOf(a).mod(modulus);
        BigInteger r = modulus;

        BigInteger t = BigInteger.ZERO;
        BigInteger newT = BigInteger.ONE;

        // Extended Euclidean Algorithm
        while (!newR.equals(BigInteger.ZERO)) {
            BigInteger q = r.divide(newR);

            BigInteger tmpT = t.subtract(q.multiply(newT));
            t = newT;
            newT = tmpT;

            BigInteger tmpR = r.subtract(q.multiply(newR));
            r = newR;
            newR = tmpR;
        }

        if (!r.equals(BigInteger.ONE)) {
            throw new ArithmeticException("Inverse does not exist since gcd(a, m) != 1");
        }

        // Ensure positive representative.
        if (t.signum() < 0) {
            t = t.add(modulus);
        }

        return t.longValueExact();
    }
}
