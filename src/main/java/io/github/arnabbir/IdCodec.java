package io.github.arnabbir;

/**
 * Encodes and decodes numeric identifiers.
 * <p>
 * Implementations must be deterministic and reversible: {@code decode(encode(x)) == x}
 * for all x in the supported domain.
 */
public interface IdCodec {

    /**
     * Encodes an internal id into an obfuscated id.
     *
     * @param id internal id
     * @return obfuscated id
     */
    long encode(long id);

    /**
     * Decodes an obfuscated id back into the internal id.
     *
     * @param id obfuscated id
     * @return internal id
     */
    long decode(long id);
}
