package io.github.arnabbir.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class ModularArithmeticTest {

    // ============= GCD Tests =============

    @Test
    void gcd_bothPositive_returnsCorrectGcd() {
        // Arrange
        long a = 48;
        long b = 18;

        // Act
        long result = ModularArithmetic.gcd(a, b);

        // Assert
        assertEquals(6, result);
    }

    @Test
    void gcd_aEqualsZero_returnsB() {
        // Arrange
        long a = 0;
        long b = 42;

        // Act
        long result = ModularArithmetic.gcd(a, b);

        // Assert
        assertEquals(42, result);
    }

    @Test
    void gcd_bEqualsZero_returnsA() {
        // Arrange
        long a = 42;
        long b = 0;

        // Act
        long result = ModularArithmetic.gcd(a, b);

        // Assert
        assertEquals(42, result);
    }

    @Test
    void gcd_bothZero_returnsZero() {
        // Arrange
        long a = 0;
        long b = 0;

        // Act
        long result = ModularArithmetic.gcd(a, b);

        // Assert
        assertEquals(0, result);
    }

    @Test
    void gcd_coprimeNumbers_returnsOne() {
        // Arrange
        long a = 7;
        long b = 11;

        // Act
        long result = ModularArithmetic.gcd(a, b);

        // Assert
        assertEquals(1, result);
    }

    @Test
    void gcd_aEqualsB_returnsA() {
        // Arrange
        long a = 25;
        long b = 25;

        // Act
        long result = ModularArithmetic.gcd(a, b);

        // Assert
        assertEquals(25, result);
    }

    @Test
    void gcd_aGreaterThanB_returnsCorrectGcd() {
        // Arrange
        long a = 100;
        long b = 35;

        // Act
        long result = ModularArithmetic.gcd(a, b);

        // Assert
        assertEquals(5, result);
    }

    @Test
    void gcd_bGreaterThanA_returnsCorrectGcd() {
        // Arrange
        long a = 35;
        long b = 100;

        // Act
        long result = ModularArithmetic.gcd(a, b);

        // Assert
        assertEquals(5, result);
    }

    @Test
    void gcd_negativeA_returnsPositiveGcd() {
        // Arrange
        long a = -48;
        long b = 18;

        // Act
        long result = ModularArithmetic.gcd(a, b);

        // Assert
        assertEquals(6, result);
    }

    @Test
    void gcd_negativeB_returnsPositiveGcd() {
        // Arrange
        long a = 48;
        long b = -18;

        // Act
        long result = ModularArithmetic.gcd(a, b);

        // Assert
        assertEquals(6, result);
    }

    @Test
    void gcd_bothNegative_returnsPositiveGcd() {
        // Arrange
        long a = -48;
        long b = -18;

        // Act
        long result = ModularArithmetic.gcd(a, b);

        // Assert
        assertEquals(6, result);
    }

    @Test
    void gcd_largeNumbers_returnsCorrectGcd() {
        // Arrange
        long a = 1000000007;
        long b = 1000000009;

        // Act
        long result = ModularArithmetic.gcd(a, b);

        // Assert
        assertEquals(1, result);
    }

    @Test
    void gcd_firstNumberIsOne_returnsOne() {
        // Arrange
        long a = 1;
        long b = 1000000;

        // Act
        long result = ModularArithmetic.gcd(a, b);

        // Assert
        assertEquals(1, result);
    }

    @Test
    void gcd_secondNumberIsOne_returnsOne() {
        // Arrange
        long a = 1000000;
        long b = 1;

        // Act
        long result = ModularArithmetic.gcd(a, b);

        // Assert
        assertEquals(1, result);
    }

    @Test
    void gcd_longMaxValue_returnsCorrectGcd() {
        // Arrange
        long a = Long.MAX_VALUE;
        long b = Long.MAX_VALUE - 1;

        // Act
        long result = ModularArithmetic.gcd(a, b);

        // Assert
        assertEquals(1, result);
    }

    @Test
    void gcd_longMinValueAndZero_throwsArithmeticException() {
        // Arrange
        long a = Long.MIN_VALUE;
        long b = 0;

        // Act & Assert
        assertThrows(
                ArithmeticException.class,
                () -> ModularArithmetic.gcd(a, b)
        );
    }

    @Test
    void gcd_zeroAndLongMinValue_throwsArithmeticException() {
        // Arrange
        long a = 0;
        long b = Long.MIN_VALUE;

        // Act & Assert
        assertThrows(
                ArithmeticException.class,
                () -> ModularArithmetic.gcd(a, b)
        );
    }

    // ============= Calculate Inverse Tests =============

    @Test
    void calculateInverse_simpleCase_returnsCorrectInverse() {
        // Arrange
        long a = 3;
        long m = 7;

        // Act
        long inverse = ModularArithmetic.calculateInverse(a, m);

        // Assert
        assertEquals(5, inverse); // 3 * 5 = 15 ≡ 1 (mod 7)
        assertEquals(1, (a * inverse) % m);
    }

    @Test
    void calculateInverse_inverseOfOne_returnsOne() {
        // Arrange
        long a = 1;
        long m = 7;

        // Act
        long inverse = ModularArithmetic.calculateInverse(a, m);

        // Assert
        assertEquals(1, inverse);
        assertEquals(1, (a * inverse) % m);
    }

    @Test
    void calculateInverse_largeCoprimeNumbers_returnsCorrectInverse() {
        // Arrange
        long a = 6364136223846793005L;
        long m = 9223372036854775783L;

        // Act
        long inverse = ModularArithmetic.calculateInverse(a, m);

        // Assert
        // Verify that (a * inverse) mod m == 1
        // Use BigInteger to avoid overflow
        assertEquals(1, java.math.BigInteger.valueOf(a)
                .multiply(java.math.BigInteger.valueOf(inverse))
                .mod(java.math.BigInteger.valueOf(m))
                .longValue());
    }

    @Test
    void calculateInverse_twoAndLargeOddNumber_returnsCorrectInverse() {
        // Arrange
        long a = 2;
        long m = 1000000007; // prime, so 2 is coprime with it

        // Act
        long inverse = ModularArithmetic.calculateInverse(a, m);

        // Assert
        assertEquals(1, (a * inverse) % m);
    }

    @Test
    void calculateInverse_negativeA_returnsValidInverse() {
        // Arrange
        long a = -3;
        long m = 7;

        // Act
        long inverse = ModularArithmetic.calculateInverse(a, m);

        // Assert
        // Both negative and positive values should work
        assertEquals(1, java.math.BigInteger.valueOf(a)
                .multiply(java.math.BigInteger.valueOf(inverse))
                .mod(java.math.BigInteger.valueOf(m))
                .longValue());
    }

    @Test
    void calculateInverse_aGreaterThanM_normalizesAndReturnsInverse() {
        // Arrange
        long a = 10; // 10 mod 7 = 3
        long m = 7;

        // Act
        long inverse = ModularArithmetic.calculateInverse(a, m);

        // Assert
        // 10 mod 7 = 3, so inverse should be same as inverse of 3
        assertEquals(1, (a * inverse) % m);
    }

    @Test
    void calculateInverse_zeroModulus_throwsIllegalArgumentException() {
        // Arrange
        long a = 3;
        long m = 0;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ModularArithmetic.calculateInverse(a, m)
        );
        assertTrue(exception.getMessage().contains("positive"));
    }

    @Test
    void calculateInverse_negativeModulus_throwsIllegalArgumentException() {
        // Arrange
        long a = 3;
        long m = -7;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ModularArithmetic.calculateInverse(a, m)
        );
        assertTrue(exception.getMessage().contains("positive"));
    }

    @Test
    void calculateInverse_nonCoprimeNumbers_throwsArithmeticException() {
        // Arrange
        long a = 6; // gcd(6, 9) = 3
        long m = 9;

        // Act & Assert
        ArithmeticException exception = assertThrows(
                ArithmeticException.class,
                () -> ModularArithmetic.calculateInverse(a, m)
        );
        assertTrue(exception.getMessage().contains("does not exist"));
    }

    @Test
    void calculateInverse_evenAWithEvenM_throwsArithmeticException() {
        // Arrange
        long a = 4;
        long m = 8; // gcd(4, 8) = 4

        // Act & Assert
        assertThrows(
                ArithmeticException.class,
                () -> ModularArithmetic.calculateInverse(a, m)
        );
    }

    @Test
    void calculateInverse_inverseIsPositive() {
        // Arrange
        long a = 3;
        long m = 7;

        // Act
        long inverse = ModularArithmetic.calculateInverse(a, m);

        // Assert
        assertTrue(inverse > 0);
        assertTrue(inverse < m);
    }

    @Test
    void calculateInverse_verificationProperty_aTimesInverseEqualsOne() {
        // Arrange
        long a = 7;
        long m = 11;

        // Act
        long inverse = ModularArithmetic.calculateInverse(a, m);

        // Assert
        assertEquals(1, (a * inverse) % m);
    }

    @Test
    void calculateInverse_modularMultiplicationProof_multipleValues() {
        // Arrange
        long[] testValues = {2, 3, 5, 7, 11, 13};
        long modulus = 17;

        // Act & Assert
        for (long a : testValues) {
            long inverse = ModularArithmetic.calculateInverse(a, modulus);
            assertEquals(1, (a * inverse) % modulus,
                    "Inverse verification failed for a=" + a);
        }
    }

    @Test
    void calculateInverse_inverseOfInverseIsOriginal() {
        // Arrange
        long a = 3;
        long m = 7;
        long inverse = ModularArithmetic.calculateInverse(a, m);

        // Act
        long inverseOfInverse = ModularArithmetic.calculateInverse(inverse, m);

        // Assert
        assertEquals(a, inverseOfInverse);
    }

    @Test
    void calculateInverse_largeModulus_returnsValidInverse() {
        // Arrange
        long a = 12345;
        long m = 1000000007;

        // Act
        long inverse = ModularArithmetic.calculateInverse(a, m);

        // Assert
        assertEquals(1, java.math.BigInteger.valueOf(a)
                .multiply(java.math.BigInteger.valueOf(inverse))
                .mod(java.math.BigInteger.valueOf(m))
                .longValue());
    }

    @Test
    void calculateInverse_aEqualsM_normalizesToZeroThenThrows() {
        // Arrange
        long a = 7;
        long m = 7; // a mod m = 0, which has no inverse

        // Act & Assert
        assertThrows(
                ArithmeticException.class,
                () -> ModularArithmetic.calculateInverse(a, m)
        );
    }

    @Test
    void calculateInverse_inverseIsInRange() {
        // Arrange
        long a = 3;
        long m = 100;

        // Act
        long inverse = ModularArithmetic.calculateInverse(a, m);

        // Assert
        assertTrue(inverse >= 0);
        assertTrue(inverse < m);
    }
}
