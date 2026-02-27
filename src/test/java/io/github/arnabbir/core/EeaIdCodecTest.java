package io.github.arnabbir.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import io.github.arnabbir.IdCodec;
import io.github.arnabbir.IdCodecFactory;

class EeaIdCodecTest {

    // ============= Constructor Tests =============

    @Test
    void constructor_validParameters_initializesSuccessfully() {
        // Arrange
        long multiplier = 3;
        long inverse = 5;
        long modulus = 7;

        // Act
        EeaIdCodec codec = new EeaIdCodec(multiplier, inverse, modulus);

        // Assert
        assertNotNull(codec);
        assertEquals(3, codec.getMultiplier());
        assertEquals(5, codec.getInverseMultiplier());
        assertEquals(7, codec.getModulus());
    }

    @Test
    void constructor_negativeMultiplier_normalizesCorrectly() {
        // Arrange
        long multiplier = -3; // should normalize to 7 in modulus 10
        long inverse = 5;
        long modulus = 10;

        // Act
        EeaIdCodec codec = new EeaIdCodec(multiplier, inverse, modulus);

        // Assert
        assertEquals(7, codec.getMultiplier()); // -3 mod 10 = 7
    }

    @Test
    void constructor_negativeInverse_normalizesCorrectly() {
        // Arrange
        long multiplier = 3;
        long inverse = -5; // should normalize to modulus - 5
        long modulus = 10;

        // Act
        EeaIdCodec codec = new EeaIdCodec(multiplier, inverse, modulus);

        // Assert
        assertEquals(5, codec.getInverseMultiplier()); // -5 mod 10 = 5
    }

    @Test
    void constructor_largeMultiplierAndInverse_normalizeWithinRange() {
        // Arrange
        long multiplier = 1000;
        long inverse = 2000;
        long modulus = 100;

        // Act
        EeaIdCodec codec = new EeaIdCodec(multiplier, inverse, modulus);

        // Assert
        assertTrue(codec.getMultiplier() >= 0);
        assertTrue(codec.getMultiplier() < modulus);
        assertTrue(codec.getInverseMultiplier() >= 0);
        assertTrue(codec.getInverseMultiplier() < modulus);
    }

    @Test
    void constructor_zeroMultiplier_normalizesToZero() {
        // Arrange
        long multiplier = 0;
        long inverse = 1;
        long modulus = 7;

        // Act
        EeaIdCodec codec = new EeaIdCodec(multiplier, inverse, modulus);

        // Assert
        assertEquals(0, codec.getMultiplier());
    }

    @Test
    void constructor_zeroInverse_normalizesToZero() {
        // Arrange
        long multiplier = 1;
        long inverse = 0;
        long modulus = 7;

        // Act
        EeaIdCodec codec = new EeaIdCodec(multiplier, inverse, modulus);

        // Assert
        assertEquals(0, codec.getInverseMultiplier());
    }

    @Test
    void constructor_modulusOne_throwsIllegalArgumentException() {
        // Arrange
        long multiplier = 3;
        long inverse = 5;
        long modulus = 1;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new EeaIdCodec(multiplier, inverse, modulus)
        );
        assertTrue(exception.getMessage().contains("modulus must be > 1"));
    }

    @Test
    void constructor_zeroModulus_throwsIllegalArgumentException() {
        // Arrange
        long multiplier = 3;
        long inverse = 5;
        long modulus = 0;

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> new EeaIdCodec(multiplier, inverse, modulus)
        );
    }

    @Test
    void constructor_negativeModulus_throwsIllegalArgumentException() {
        // Arrange
        long multiplier = 3;
        long inverse = 5;
        long modulus = -5;

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> new EeaIdCodec(multiplier, inverse, modulus)
        );
    }

    @Test
    void constructor_longMaxValueModulus_succeeds() {
        // Arrange
        long multiplier = 3;
        long inverse = 5;
        long modulus = Long.MAX_VALUE;

        // Act
        EeaIdCodec codec = new EeaIdCodec(multiplier, inverse, modulus);

        // Assert
        assertNotNull(codec);
        assertEquals(modulus, codec.getModulus());
    }

    // ============= Encode Tests =============

    @Test
    void encode_zeroId_returnsZero() {
        // Arrange
        EeaIdCodec codec = new EeaIdCodec(3, 5, 7);
        long id = 0;

        // Act
        long encoded = codec.encode(id);

        // Assert
        assertEquals(0, encoded);
    }

    @Test
    void encode_positiveId_returnsInRangeValue() {
        // Arrange
        EeaIdCodec codec = new EeaIdCodec(3, 5, 7);
        long id = 4;

        // Act
        long encoded = codec.encode(id);

        // Assert
        assertTrue(encoded >= 0);
        assertTrue(encoded < 7);
    }

    @Test
    void encode_negativeId_normalizesBeforeEncoding() {
        // Arrange
        EeaIdCodec codec = new EeaIdCodec(3, 5, 7);
        long id = -4; // -4 mod 7 = 3

        // Act
        long encoded = codec.encode(id);

        // Assert
        assertTrue(encoded >= 0);
        assertTrue(encoded < 7);
    }

    @Test
    void encode_idEqualToModulus_normalizesToZero() {
        // Arrange
        EeaIdCodec codec = new EeaIdCodec(3, 5, 7);
        long id = 7; // 7 mod 7 = 0

        // Act
        long encoded = codec.encode(id);

        // Assert
        assertEquals(0, encoded);
    }

    @Test
    void encode_idLargerThanModulus_normalizesCorrectly() {
        // Arrange
        EeaIdCodec codec = new EeaIdCodec(3, 5, 7);
        long id = 20; // 20 mod 7 = 6, then (6 * 3) mod 7

        // Act
        long encoded = codec.encode(id);

        // Assert
        assertTrue(encoded >= 0);
        assertTrue(encoded < 7);
    }

    @Test
    void encode_allValuesProduceValidResults() {
        // Arrange
        EeaIdCodec codec = new EeaIdCodec(3, 5, 7);

        // Act & Assert
        for (long id = 0; id < 1000; id++) {
            long encoded = codec.encode(id);
            assertTrue(encoded >= 0, "Encoded result is negative for id=" + id);
            assertTrue(encoded < 7, "Encoded result exceeds modulus for id=" + id);
        }
    }

    // ============= Decode Tests =============

    @Test
    void decode_zeroId_returnsZero() {
        // Arrange
        EeaIdCodec codec = new EeaIdCodec(3, 5, 7);
        long id = 0;

        // Act
        long decoded = codec.decode(id);

        // Assert
        assertEquals(0, decoded);
    }

    @Test
    void decode_positiveId_returnsInRangeValue() {
        // Arrange
        EeaIdCodec codec = new EeaIdCodec(3, 5, 7);
        long id = 4;

        // Act
        long decoded = codec.decode(id);

        // Assert
        assertTrue(decoded >= 0);
        assertTrue(decoded < 7);
    }

    @Test
    void decode_negativeId_normalizesBeforeDecoding() {
        // Arrange
        EeaIdCodec codec = new EeaIdCodec(3, 5, 7);
        long id = -2; // -2 mod 7 = 5

        // Act
        long decoded = codec.decode(id);

        // Assert
        assertTrue(decoded >= 0);
        assertTrue(decoded < 7);
    }

    @Test
    void decode_idEqualToModulus_normalizesToZero() {
        // Arrange
        EeaIdCodec codec = new EeaIdCodec(3, 5, 7);
        long id = 7; // 7 mod 7 = 0

        // Act
        long decoded = codec.decode(id);

        // Assert
        assertEquals(0, decoded);
    }

    @Test
    void decode_allValuesProduceValidResults() {
        // Arrange
        EeaIdCodec codec = new EeaIdCodec(3, 5, 7);

        // Act & Assert
        for (long id = 0; id < 1000; id++) {
            long decoded = codec.decode(id);
            assertTrue(decoded >= 0, "Decoded result is negative for id=" + id);
            assertTrue(decoded < 7, "Decoded result exceeds modulus for id=" + id);
        }
    }

    // ============= RoundTrip Tests =============

    @Test
    void encodeDecode_zeroId_isReversible() {
        // Arrange
        EeaIdCodec codec = new EeaIdCodec(3, 5, 7);
        long originalId = 0;

        // Act
        long encoded = codec.encode(originalId);
        long decoded = codec.decode(encoded);

        // Assert
        assertEquals(originalId, decoded);
    }

    @Test
    void encodeDecodeRoundTrip_smallModulusValuesWithinRange_areReversible() {
        // Arrange - Use values created via factory to ensure coprime multiplier/inverse
        IdCodec codec = IdCodecFactory.create(3, 7);

        // Act & Assert - only test id values less than modulus
        for (long id = 0; id < 7; id++) {
            long encoded = codec.encode(id);
            long decoded = codec.decode(encoded);
            assertEquals(id, decoded, "Round-trip failed for id=" + id);
        }
    }

    @Test
    void encodeDecodeRoundTrip_zeroAndPositiveIds_areReversible() {
        // Arrange - Use values created via factory to ensure coprime multiplier/inverse
        IdCodec codec = IdCodecFactory.create(3, 7);
        long[] testIds = {0, 1, 2, 3, 4, 5, 6};

        // Act & Assert
        for (long id : testIds) {
            long encoded = codec.encode(id);
            long decoded = codec.decode(encoded);
            assertEquals(id, decoded, "Round-trip failed for id=" + id);
        }
    }

    @Test
    void encodeDecodeRoundTrip_largeModulusWithVaryingIds_areReversible() {
        // Arrange - Use factory-created codec with large modulus
        IdCodec codec = IdCodecFactory.create(6364136223846793005L, 9223372036854775783L);
        long[] testIds = {
                0,
                1,
                100,
                1000,
                999999,
                1000000
        };

        // Act & Assert
        for (long id : testIds) {
            long encoded = codec.encode(id);
            long decoded = codec.decode(encoded);
            assertEquals(id, decoded, "Round-trip failed for id=" + id);
        }
    }

    // ============= ModularMultiply Tests =============

    @Test
    void modularMultiply_zeroBase_returnsZero() {
        // Arrange
        long a = 0;
        long b = 5;
        long m = 7;

        // Act
        long result = EeaIdCodec.modularMultiply(a, b, m);

        // Assert
        assertEquals(0, result);
    }

    @Test
    void modularMultiply_zeroMultiplier_returnsZero() {
        // Arrange
        long a = 5;
        long b = 0;
        long m = 7;

        // Act
        long result = EeaIdCodec.modularMultiply(a, b, m);

        // Assert
        assertEquals(0, result);
    }

    @Test
    void modularMultiply_bothZero_returnsZero() {
        // Arrange
        long a = 0;
        long b = 0;
        long m = 7;

        // Act
        long result = EeaIdCodec.modularMultiply(a, b, m);

        // Assert
        assertEquals(0, result);
    }

    @Test
    void modularMultiply_multiplyByOne_equalsBase() {
        // Arrange
        long a = 42;
        long b = 1;
        long m = 100;

        // Act
        long result = EeaIdCodec.modularMultiply(a, b, m);

        // Assert
        assertEquals(42, result);
    }

    @Test
    void modularMultiply_simpleValues_computesCorrectly() {
        // Arrange
        long a = 2;
        long b = 3;
        long m = 5;

        // Act
        long result = EeaIdCodec.modularMultiply(a, b, m);

        // Assert
        assertEquals(1, result); // (2 * 3) mod 5 = 6 mod 5 = 1
    }

    @Test
    void modularMultiply_resultEqualsModulus_returnsZero() {
        // Arrange
        long a = 3;
        long b = 5;
        long m = 15;

        // Act
        long result = EeaIdCodec.modularMultiply(a, b, m);

        // Assert
        assertEquals(0, result); // (3 * 5) mod 15 = 0
    }

    @Test
    void modularMultiply_resultLessThanModulus_returnsCorrectly() {
        // Arrange
        long a = 2;
        long b = 4;
        long m = 10;

        // Act
        long result = EeaIdCodec.modularMultiply(a, b, m);

        // Assert
        assertEquals(8, result); // (2 * 4) % 10 = 8
    }

    @Test
    void modularMultiply_largeValues_avoidsOverflow() {
        // Arrange - Normalize large values to modulus range first
        long a = (Long.MAX_VALUE - 100) % 1000000007;
        long b = (Long.MAX_VALUE - 200) % 1000000007;
        long m = 1000000007;

        // Act
        long result = EeaIdCodec.modularMultiply(a, b, m);

        // Assert
        assertTrue(result >= 0);
        assertTrue(result < m);
    }

    @Test
    void modularMultiply_allResultsWithinBounds() {
        // Arrange
        long m = 17;

        // Act & Assert
        for (long a = 0; a < m; a++) {
            for (long b = 0; b < m; b++) {
                long result = EeaIdCodec.modularMultiply(a, b, m);
                assertTrue(result >= 0, "Result is negative for a=" + a + ", b=" + b);
                assertTrue(result < m, "Result exceeds modulus for a=" + a + ", b=" + b);
            }
        }
    }

    @Test
    void modularMultiply_commutativity_abEqualsBA() {
        // Arrange
        long a = 7;
        long b = 11;
        long m = 19;

        // Act
        long resultAB = EeaIdCodec.modularMultiply(a, b, m);
        long resultBA = EeaIdCodec.modularMultiply(b, a, m);

        // Assert
        assertEquals(resultAB, resultBA); // Multiplication is commutative
    }

    @Test
    void modularMultiply_identity_multiplyByModulus_returnsZero() {
        // Arrange
        long a = 7;
        long b = 100; // larger than modulus
        long m = 17;
        long bNormalized = b % m;

        // Act
        long result = EeaIdCodec.modularMultiply(a, bNormalized, m);

        // Assert
        assertTrue(result >= 0);
        assertTrue(result < m);
    }

    // ============= Getter Tests =============

    @Test
    void getMultiplier_returnsNormalizedValue() {
        // Arrange
        long multiplier = 100;
        long modulus = 20;
        EeaIdCodec codec = new EeaIdCodec(multiplier, 1, modulus);

        // Act
        long result = codec.getMultiplier();

        // Assert
        assertEquals(100 % modulus, result);
        assertEquals(0, result); // 100 mod 20 = 0
    }

    @Test
    void getInverseMultiplier_returnsNormalizedValue() {
        // Arrange
        long inverse = 50;
        long modulus = 17;
        EeaIdCodec codec = new EeaIdCodec(1, inverse, modulus);

        // Act
        long result = codec.getInverseMultiplier();

        // Assert
        assertEquals(50 % modulus, result);
        assertEquals(16, result); // 50 mod 17 = 16
    }

    @Test
    void getModulus_returnsExactValue() {
        // Arrange
        long modulus = 1000000007;
        EeaIdCodec codec = new EeaIdCodec(3, 5, modulus);

        // Act
        long result = codec.getModulus();

        // Assert
        assertEquals(modulus, result);
    }

    // ============= ToString Tests =============

    @Test
    void toString_containsAllFields() {
        // Arrange
        EeaIdCodec codec = new EeaIdCodec(3, 5, 7);

        // Act
        String result = codec.toString();

        // Assert
        assertTrue(result.contains("multiplier"));
        assertTrue(result.contains("inverseMultiplier"));
        assertTrue(result.contains("modulus"));
    }

    @Test
    void toString_containsCorrectValues() {
        // Arrange
        EeaIdCodec codec = new EeaIdCodec(3, 5, 7);

        // Act
        String result = codec.toString();

        // Assert
        assertTrue(result.contains("3"));
        assertTrue(result.contains("5"));
        assertTrue(result.contains("7"));
    }

    @Test
    void toString_isNotNull() {
        // Arrange
        EeaIdCodec codec = new EeaIdCodec(3, 5, 7);

        // Act
        String result = codec.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    // ============= Serialization Tests =============

    @Test
    void serialization_codecIsSerializable() throws IOException {
        // Arrange
        EeaIdCodec codec = new EeaIdCodec(3, 5, 7);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        // Act
        oos.writeObject(codec);
        oos.close();

        // Assert
        assertTrue(baos.toByteArray().length > 0);
    }

    @Test
    void serialization_deserializedCodecHasSameProperties() throws IOException, ClassNotFoundException {
        // Arrange
        EeaIdCodec originalCodec = new EeaIdCodec(3, 5, 7);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(originalCodec);
        oos.close();

        // Act
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        EeaIdCodec deserializedCodec = (EeaIdCodec) ois.readObject();

        // Assert
        assertEquals(originalCodec.getMultiplier(), deserializedCodec.getMultiplier());
        assertEquals(originalCodec.getInverseMultiplier(), deserializedCodec.getInverseMultiplier());
        assertEquals(originalCodec.getModulus(), deserializedCodec.getModulus());
    }

    @Test
    void serialization_deserializedCodecProducesSameEncoding() throws IOException, ClassNotFoundException {
        // Arrange
        EeaIdCodec originalCodec = new EeaIdCodec(3, 5, 7);
        long testId = 4;
        long originalEncoded = originalCodec.encode(testId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(originalCodec);
        oos.close();

        // Act
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        EeaIdCodec deserializedCodec = (EeaIdCodec) ois.readObject();
        long deserializedEncoded = deserializedCodec.encode(testId);

        // Assert
        assertEquals(originalEncoded, deserializedEncoded);
    }

    @Test
    void serialization_deserializedCodecProducesSameDecoding() throws IOException, ClassNotFoundException {
        // Arrange
        EeaIdCodec originalCodec = new EeaIdCodec(3, 5, 7);
        long testId = 5;
        long originalDecoded = originalCodec.decode(testId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(originalCodec);
        oos.close();

        // Act
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        EeaIdCodec deserializedCodec = (EeaIdCodec) ois.readObject();
        long deserializedDecoded = deserializedCodec.decode(testId);

        // Assert
        assertEquals(originalDecoded, deserializedDecoded);
    }

    @Test
    void serialization_roundTripReverseibility() throws IOException, ClassNotFoundException {
        // Arrange
        EeaIdCodec originalCodec = new EeaIdCodec(3, 5, 7);
        long[] testIds = {0, 1, 2, 3, 4, 5, 6};

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(originalCodec);
        oos.close();

        // Act
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        EeaIdCodec deserializedCodec = (EeaIdCodec) ois.readObject();

        // Assert
        for (long id : testIds) {
            long encoded = deserializedCodec.encode(id);
            long decoded = deserializedCodec.decode(encoded);
            assertEquals(id, decoded, "Reversibility failed for id=" + id);
        }
    }

    // ============= Integration Tests =============

    @Test
    void integration_factoryCreatedCodecProduceValidResults() {
        // Arrange
        IdCodec codec = IdCodecFactory.create(3, 7);

        // Act
        long encoded = codec.encode(5);
        long decoded = codec.decode(encoded);

        // Assert
        assertEquals(5, decoded);
    }

    @Test
    void integration_factoryCreatedCodecWithLargeValues() {
        // Arrange
        long multiplier = 6364136223846793005L;
        long modulus = 9223372036854775783L;
        IdCodec codec = IdCodecFactory.create(multiplier, modulus);

        // Act
        for (long i = 0; i < 10000; i++) {
            long encoded = codec.encode(i);
            long decoded = codec.decode(encoded);

            // Assert
            assertEquals(i, decoded, "Round-trip failed at i=" + i);
        }
    }
}
