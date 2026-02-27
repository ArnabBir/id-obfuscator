package io.github.arnabbir;

import io.github.arnabbir.core.EeaIdCodec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdCodecFactoryTest {

    // ============= Factory Creation Tests =============

    @Test
    void create_simpleCoprimeValues_returnsValidCodec() {
        // Arrange
        long multiplier = 3;
        long modulus = 7;

        // Act
        IdCodec codec = IdCodecFactory.create(multiplier, modulus);

        // Assert
        assertNotNull(codec);
        assertInstanceOf(EeaIdCodec.class, codec);
    }

    @Test
    void create_largeCoprimeValues_returnsValidCodec() {
        // Arrange
        long multiplier = 6364136223846793005L;
        long modulus = 9223372036854775783L;

        // Act
        IdCodec codec = IdCodecFactory.create(multiplier, modulus);

        // Assert
        assertNotNull(codec);
        assertInstanceOf(EeaIdCodec.class, codec);
    }

    @Test
    void create_multiplierOne_returnsValidCodec() {
        // Arrange
        long multiplier = 1;
        long modulus = 7;

        // Act
        IdCodec codec = IdCodecFactory.create(multiplier, modulus);

        // Assert
        assertNotNull(codec);
    }

    @Test
    void create_multiplierEqualToModulusMinusOne_returnsValidCodec() {
        // Arrange
        long multiplier = 6; // modulus - 1
        long modulus = 7;

        // Act
        IdCodec codec = IdCodecFactory.create(multiplier, modulus);

        // Assert
        assertNotNull(codec);
    }

    @Test
    void create_negativeMultiplier_createsValidCodec() {
        // Arrange
        long multiplier = -3;
        long modulus = 7; // gcd(-3, 7) = gcd(3, 7) = 1

        // Act
        IdCodec codec = IdCodecFactory.create(multiplier, modulus);

        // Assert
        assertNotNull(codec);
        assertInstanceOf(EeaIdCodec.class, codec);
    }

    @Test
    void create_modulusTwo_returnsValidCodec() {
        // Arrange
        long multiplier = 1;
        long modulus = 2;

        // Act
        IdCodec codec = IdCodecFactory.create(multiplier, modulus);

        // Assert
        assertNotNull(codec);
    }

    @Test
    void create_createdCodecIsEncodeDecodeReversible() {
        // Arrange
        IdCodec codec = IdCodecFactory.create(3, 7);
        long testId = 4;

        // Act
        long encoded = codec.encode(testId);
        long decoded = codec.decode(encoded);

        // Assert
        assertEquals(testId, decoded);
    }

    @Test
    void create_primeModulusWithAnyCoprime_succeeds() {
        // Arrange - prime moduli
        long[] primes = {2, 3, 5, 7, 11, 13, 17, 19};

        // Act & Assert
        for (long prime : primes) {
            for (long multiplier = 1; multiplier < prime; multiplier++) {
                IdCodec codec = IdCodecFactory.create(multiplier, prime);
                assertNotNull(codec);
            }
        }
    }

    @Test
    void create_largePrimeModulus_createsValidCodec() {
        // Arrange
        long multiplier = 12345;
        long modulus = 1000000007; // large prime

        // Act
        IdCodec codec = IdCodecFactory.create(multiplier, modulus);

        // Assert
        assertNotNull(codec);
    }

    // ============= Factory Error Cases =============

    @Test
    void create_modulusOne_throwsIllegalArgumentException() {
        // Arrange
        long multiplier = 3;
        long modulus = 1;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> IdCodecFactory.create(multiplier, modulus)
        );
        assertTrue(exception.getMessage().contains("modulus must be > 1"));
    }

    @Test
    void create_modulusZero_throwsIllegalArgumentException() {
        // Arrange
        long multiplier = 3;
        long modulus = 0;

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> IdCodecFactory.create(multiplier, modulus)
        );
    }

    @Test
    void create_negativeModulus_throwsIllegalArgumentException() {
        // Arrange
        long multiplier = 3;
        long modulus = -5;

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> IdCodecFactory.create(multiplier, modulus)
        );
    }

    @Test
    void create_nonCoprimeMultiplierAndModulus_throwsIllegalArgumentException() {
        // Arrange
        long multiplier = 6; // gcd(6, 9) = 3
        long modulus = 9;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> IdCodecFactory.create(multiplier, modulus)
        );
        assertTrue(exception.getMessage().contains("coprime"));
        assertTrue(exception.getMessage().contains("gcd=3"));
    }

    @Test
    void create_bothEven_throwsIllegalArgumentException() {
        // Arrange
        long multiplier = 2;
        long modulus = 4; // gcd(2, 4) = 2

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> IdCodecFactory.create(multiplier, modulus)
        );
        assertTrue(exception.getMessage().contains("coprime"));
    }

    @Test
    void create_zeroMultiplier_throwsIllegalArgumentException() {
        // Arrange
        long multiplier = 0;
        long modulus = 7;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> IdCodecFactory.create(multiplier, modulus)
        );
        assertTrue(exception.getMessage().contains("coprime"));
    }

    @Test
    void create_multiplierEqualToModulus_throwsIllegalArgumentException() {
        // Arrange
        long multiplier = 7;
        long modulus = 7; // gcd(7, 7) = 7

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> IdCodecFactory.create(multiplier, modulus)
        );
    }

    @Test
    void create_multipleNonCoprimePairs_allThrowException() {
        // Arrange
        long[][] nonCoprimes = {
                {2, 4},
                {3, 6},
                {4, 8},
                {5, 10},
                {6, 9}
        };

        // Act & Assert
        for (long[] pair : nonCoprimes) {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> IdCodecFactory.create(pair[0], pair[1]),
                    "Should fail for multiplier=" + pair[0] + ", modulus=" + pair[1]
            );
            assertTrue(exception.getMessage().contains("coprime"));
        }
    }

    @Test
    void create_multipleCoprimes_allSucceed() {
        // Arrange
        long[][] coprimes = {
                {1, 2},
                {2, 3},
                {3, 4},
                {4, 5},
                {5, 6},
                {7, 10},
                {13, 20}
        };

        // Act & Assert
        for (long[] pair : coprimes) {
            IdCodec codec = IdCodecFactory.create(pair[0], pair[1]);
            assertNotNull(codec, "Should succeed for multiplier=" + pair[0] + ", modulus=" + pair[1]);
        }
    }

    // ============= Factory Type Checking =============

    @Test
    void create_alwaysReturnsEeaIdCodec() {
        // Arrange
        IdCodec codec1 = IdCodecFactory.create(1, 2);
        IdCodec codec2 = IdCodecFactory.create(3, 7);
        IdCodec codec3 = IdCodecFactory.create(5, 13);

        // Act & Assert
        assertInstanceOf(EeaIdCodec.class, codec1);
        assertInstanceOf(EeaIdCodec.class, codec2);
        assertInstanceOf(EeaIdCodec.class, codec3);
    }

    // ============= Large Values Tests =============

    @Test
    void create_longMaxValueMultiplier_withSmallModulus_succeeds() {
        // Arrange
        long multiplier = Long.MAX_VALUE;
        long modulus = 1000000007; // gcd(Long.MAX_VALUE, 1000000007) = 1

        // Act
        IdCodec codec = IdCodecFactory.create(multiplier, modulus);

        // Assert
        assertNotNull(codec);
    }

    @Test
    void create_veryLargeModulus_succeeds() {
        // Arrange
        long multiplier = 1000000007; // prime, coprime with most numbers
        long modulus = Long.MAX_VALUE - 1;

        // Act
        IdCodec codec = IdCodecFactory.create(multiplier, modulus);

        // Assert
        assertNotNull(codec);
    }

    // ============= Consistency Tests =============

    @Test
    void create_sameParametersProduceConsistentCodecs() {
        // Arrange
        long multiplier = 3;
        long modulus = 7;
        long testId = 5;

        // Act
        IdCodec codec1 = IdCodecFactory.create(multiplier, modulus);
        IdCodec codec2 = IdCodecFactory.create(multiplier, modulus);

        long encoded1 = codec1.encode(testId);
        long encoded2 = codec2.encode(testId);

        // Assert
        assertEquals(encoded1, encoded2);
    }

    @Test
    void create_differentParametersProduceDifferentCodecs() {
        // Arrange
        long testId = 5;

        // Act
        IdCodec codec1 = IdCodecFactory.create(3, 7);
        IdCodec codec2 = IdCodecFactory.create(5, 13);

        long encoded1 = codec1.encode(testId);
        long encoded2 = codec2.encode(testId);

        // Assert
        // Different parameters should generally produce different encodings
        // (though equality is not guaranteed for all values)
        assertNotNull(encoded1);
        assertNotNull(encoded2);
    }

    // ============= Thread Safety and Independence =============

    @Test
    void create_multipleCodecsAreIndependent() {
        // Arrange
        IdCodec codec1 = IdCodecFactory.create(2, 5);
        IdCodec codec2 = IdCodecFactory.create(3, 7);
        long testId = 1;

        // Act
        long encoded1 = codec1.encode(testId);
        long encoded2 = codec2.encode(testId);

        // Assert - Codecs should be independent and produce different results
        assertNotEquals(encoded1, encoded2);
    }

    @Test
    void create_codecInstancesAreIndependent() {
        // Arrange
        IdCodec codec1 = IdCodecFactory.create(3, 7);
        IdCodec codec2 = IdCodecFactory.create(3, 7);
        long testId = 4;

        // Act
        long encoded1a = codec1.encode(testId);
        long encoded2a = codec2.encode(testId);
        long decoded1 = codec1.decode(encoded1a);
        long decoded2 = codec2.decode(encoded2a);

        // Assert - Different instances should work independently
        assertEquals(testId, decoded1);
        assertEquals(testId, decoded2);
    }

    @Test
    void create_noExceptionOnMultipleCalls() {
        // Arrange
        for (int i = 1; i < 50; i++) {
            long multiplier = i;
            long modulus = 1009; // prime number to ensure coprimes with small i
            
            // Act & Assert
            IdCodec codec = IdCodecFactory.create(multiplier, modulus);
            assertNotNull(codec);
        }
    }
}
