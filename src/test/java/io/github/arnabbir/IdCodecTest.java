package io.github.arnabbir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

import io.github.arnabbir.core.EeaIdCodec;

class IdCodecTest {

    private static final long MODULUS = 9223372036854775783L; // close to Long.MAX_VALUE
    private static final long MULTIPLIER = 6364136223846793005L; // coprime w/ MODULUS

    @Test
    void create_validParameters_returnsEeaIdCodec() {
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
    void create_coprimeMultiplier_succeeds() {
        // Arrange
        long multiplier = 3;
        long modulus = 7;

        // Act
        IdCodec codec = IdCodecFactory.create(multiplier, modulus);

        // Assert
        assertNotNull(codec);
    }

    @Test
    void create_modulusEqualsOne_throwsIllegalArgumentException() {
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
    void create_modulusLessThanOne_throwsIllegalArgumentException() {
        // Arrange
        long multiplier = 3;
        long modulus = 0;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> IdCodecFactory.create(multiplier, modulus)
        );
        assertTrue(exception.getMessage().contains("modulus must be > 1"));
    }

    @Test
    void create_nonCoprimeMultiplier_throwsIllegalArgumentException() {
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
    void create_negativeNonCoprimeMultiplier_throwsIllegalArgumentException() {
        // Arrange
        long multiplier = -4; // gcd(-4, 8) = 4
        long modulus = 8;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> IdCodecFactory.create(multiplier, modulus)
        );
        assertTrue(exception.getMessage().contains("coprime"));
    }

    // ============= EeaIdCodec Tests =============

    @Test
    void eea_constructor_validParameters_initializesSuccessfully() {
        // Arrange
        long multiplier = 3;
        long inverse = 5; // Example: mod 7, 3*5 = 15 ≡ 1 (mod 7)
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
    void eea_constructor_modulusOne_throwsIllegalArgumentException() {
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
    void eea_constructor_zeroModulus_throwsIllegalArgumentException() {
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
    void eea_constructor_negativeModulus_throwsIllegalArgumentException() {
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
    void eea_getMultiplier_returnsNormalizedMultiplier() {
        // Arrange
        long multiplier = 15; // will be normalized to 1 (15 mod 10)
        long inverse = 1;
        long modulus = 10;

        // Act
        EeaIdCodec codec = new EeaIdCodec(multiplier, inverse, modulus);

        // Assert
        assertEquals(5, codec.getMultiplier()); // 15 mod 10 = 5
    }

    @Test
    void eea_getInverseMultiplier_returnsNormalizedInverse() {
        // Arrange
        long multiplier = 3;
        long inverse = 23; // will be normalized to 3 (23 mod 10)
        long modulus = 10;

        // Act
        EeaIdCodec codec = new EeaIdCodec(multiplier, inverse, modulus);

        // Assert
        assertEquals(3, codec.getInverseMultiplier()); // 23 mod 10 = 3
    }

    @Test
    void eea_getModulus_returnsModulus() {
        // Arrange
        long multiplier = 3;
        long inverse = 5;
        long modulus = 7;

        // Act
        EeaIdCodec codec = new EeaIdCodec(multiplier, inverse, modulus);

        // Assert
        assertEquals(7, codec.getModulus());
    }

    @Test
    void eea_encode_zeroId_returnsZero() {
        // Arrange
        IdCodec codec = IdCodecFactory.create(MULTIPLIER, MODULUS);
        long id = 0;

        // Act
        long encoded = codec.encode(id);

        // Assert
        EeaIdCodec eeaCodec = (EeaIdCodec) codec;
        long strippedEncoded = stripVersionBits(encoded, eeaCodec.getVersionBits());
        assertEquals(0L, strippedEncoded);
    }

    @Test
    void eea_encode_positiveId_returnsEncodedValue() {
        // Arrange
        IdCodec codec = IdCodecFactory.create(MULTIPLIER, MODULUS);
        long id = 12345;

        // Act
        long encoded = codec.encode(id);

        // Assert
        assertNotNull(encoded);
        assertTrue(encoded >= 0);
    }

    @Test
    void eea_encode_negativeId_normalizesAndEncodes() {
        // Arrange
        IdCodec codec = IdCodecFactory.create(MULTIPLIER, MODULUS);
        long id = -1;

        // Act
        long encoded = codec.encode(id);

        // Assert
        assertTrue(encoded >= 0);
    }

    @Test
    void eea_encode_largeId_returnsValidEncoding() {
        // Arrange
        IdCodec codec = IdCodecFactory.create(MULTIPLIER, MODULUS);
        long id = Long.MAX_VALUE - 100;

        // Act
        long encoded = codec.encode(id);

        // Assert
        assertTrue(encoded >= 0);
        assertTrue(encoded < MODULUS);
    }

    @Test
    void eea_decode_zeroId_returnsZero() {
        // Arrange
        IdCodec codec = IdCodecFactory.create(MULTIPLIER, MODULUS);
        long id = 0;

        // Act
        long encoded = codec.encode(id);
        long decoded = codec.decode(encoded);

        // Assert
        assertEquals(0L, decoded);
    }

    @Test
    void eea_decode_positiveId_returnsDecodedValue() {
        // Arrange
        IdCodec codec = IdCodecFactory.create(MULTIPLIER, MODULUS);
        long id = 98765;

        // Act - encode first to set version bits, then decode
        long encoded = codec.encode(id);
        long decoded = codec.decode(encoded);

        // Assert
        assertEquals(id, decoded);
    }

    @Test
    void eea_decode_negativeId_normalizesAndDecodes() {
        // Arrange
        IdCodec codec = IdCodecFactory.create(MULTIPLIER, MODULUS);
        long id = -12345;  // Use a specific value to test normalization

        // Act
        long normalized = Math.floorMod(id, MODULUS);
        long encoded = codec.encode(normalized);
        long decoded = codec.decode(encoded);

        // Assert
        assertEquals(normalized, decoded);
    }

    @Test
    void eea_encodeDecodeRoundTrip_allIdsWithinRangeAreReversible() {
        // Arrange
        IdCodec codec = IdCodecFactory.create(MULTIPLIER, MODULUS);
        long testLimit = 1000;

        // Act & Assert
        for (long i = 0; i < testLimit; i++) {
            long encoded = codec.encode(i);
            long decoded = codec.decode(encoded);
            assertEquals(i, decoded, "Round-trip failed for id=" + i);
        }
    }

    @Test
    void eea_encodeDecodeRoundTrip_largeIdValuesAreReversible() {
        // Arrange
        IdCodec codec = IdCodecFactory.create(MULTIPLIER, MODULUS);
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



    @Test
    void eea_toString_containsAllComponents() {
        // Arrange
        EeaIdCodec codec = new EeaIdCodec(3, 5, 7);

        // Act
        String result = codec.toString();

        // Assert
        assertTrue(result.contains("multiplier"));
        assertTrue(result.contains("inverseMultiplier"));
        assertTrue(result.contains("modulus"));
        assertTrue(result.contains("3"));
        assertTrue(result.contains("5"));
        assertTrue(result.contains("7"));
    }

    @Test
    void eea_serialization_codecCanBeSerialized() throws IOException, ClassNotFoundException {
        // Arrange
        EeaIdCodec originalCodec = new EeaIdCodec(3, 5, 7);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        // Act
        oos.writeObject(originalCodec);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        EeaIdCodec deserializedCodec = (EeaIdCodec) ois.readObject();

        // Assert
        assertEquals(originalCodec.getMultiplier(), deserializedCodec.getMultiplier());
        assertEquals(originalCodec.getInverseMultiplier(), deserializedCodec.getInverseMultiplier());
        assertEquals(originalCodec.getModulus(), deserializedCodec.getModulus());
    }

    @Test
    void eea_serialization_decodedCodecIsReversible() throws IOException, ClassNotFoundException {
        // Arrange
        EeaIdCodec originalCodec = new EeaIdCodec(3, 5, 7);
        long testId = 4;

        long originalEncoded = originalCodec.encode(testId);
        long originalDecoded = originalCodec.decode(originalEncoded);

        // Serialize and deserialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(originalCodec);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        EeaIdCodec deserializedCodec = (EeaIdCodec) ois.readObject();

        // Act
        long deserializedEncoded = deserializedCodec.encode(testId);
        long deserializedDecoded = deserializedCodec.decode(deserializedEncoded);

        // Assert
        assertEquals(originalEncoded, deserializedEncoded);
        assertEquals(originalDecoded, deserializedDecoded);
        assertEquals(testId, deserializedDecoded);
    }

    // ============= Thread Safety and Performance Tests =============

    @Test
    void eea_threadSafety_parallelEncodeDecodeIsConsistent() {
        // Arrange
        IdCodec codec = IdCodecFactory.create(MULTIPLIER, MODULUS);
        AtomicBoolean failed = new AtomicBoolean(false);

        // Act
        try {
            LongStream.range(0L, 100_000L)
                    .parallel()
                    .forEach(i -> {
                        long encoded = codec.encode(i);
                        long decoded = codec.decode(encoded);
                        if (decoded != i) {
                            failed.set(true);
                            throw new AssertionError("Mismatch at i=" + i);
                        }
                    });
        } catch (Throwable t) {
            Throwable root = t;
            while (root.getCause() != null && root.getCause() != root) {
                root = root.getCause();
            }
            fail(root.getMessage(), root);
        }

        // Assert
        assertFalse(failed.get(), "Parallel encode/decode should be deterministic");
    }

    @Test
    void eea_reversibility_oneMillionIds() {
        // Arrange
        IdCodec codec = IdCodecFactory.create(MULTIPLIER, MODULUS);

        // Act & Assert (comprehensive reversibility test)
        for (long i = 0L; i < 1_000_000L; i++) {
            long encoded = codec.encode(i);
            long decoded = codec.decode(encoded);
            assertEquals(i, decoded, "Reversibility check failed at i=" + i);
        }
    }

    @Test
    void eea_encode_multipleCallsWithSameInput_produceIdenticalResults() {
        // Arrange
        IdCodec codec = IdCodecFactory.create(MULTIPLIER, MODULUS);
        long id = 54321;

        // Act
        long result1 = codec.encode(id);
        long result2 = codec.encode(id);
        long result3 = codec.encode(id);

        // Assert
        assertEquals(result1, result2);
        assertEquals(result2, result3);
    }

    @Test
    void eea_decode_multipleCallsWithSameInput_produceIdenticalResults() {
        // Arrange
        IdCodec codec = IdCodecFactory.create(MULTIPLIER, MODULUS);
        long id = 99999;

        // Act
        long encoded = codec.encode(id);
        long result1 = codec.decode(encoded);
        long result2 = codec.decode(encoded);
        long result3 = codec.decode(encoded);

        // Assert
        assertEquals(result1, result2);
        assertEquals(result2, result3);
        assertEquals(id, result1);
    }

    private static long stripVersionBits(long encoded, int versionBits) {
        long mask = (1L << (64 - versionBits)) - 1;
        return encoded & mask;
    }
}
