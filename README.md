# id-obfuscator

Deterministic, reversible obfuscation for sequential numeric IDs using modular multiplicative inverses.

> This is **not encryption**. It is a fast, one to one permutation over the range `[0, modulus)`.

## Install (Maven)

```xml
<dependency>
  <groupId>io.github.arnabbir</groupId>
  <artifactId>id-obfuscator</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Quick start

```java
import io.github.arnabbir.IdCodec;
import io.github.arnabbir.IdCodecFactory;

public class Demo {
  public static void main(String[] args) {
    long modulus = 9_223_372_036_854_775_783L;
    long multiplier = 6_364_136_223_846_793_005L;

    IdCodec codec = IdCodecFactory.create(multiplier, modulus);

    long internalId = 12345L;
    long publicId = codec.encode(internalId);
    long roundTrip = codec.decode(publicId);

    System.out.println(publicId);
    System.out.println(roundTrip); // 12345
  }
}
```

## Choosing `multiplier` and `modulus`

### Requirements

- `modulus` must be `> 1`.
- `multiplier` and `modulus` must be coprime (`gcd(multiplier, modulus) == 1`).
- The codec is a permutation of the set `{0, 1, ..., modulus-1}`.
  - If you only ever encode database IDs `0 <= id < modulus`, then decode will always restore the original.
  - If you encode an ID outside that range, the operation works on `id mod modulus`.

### Recommended Values

#### For typical web applications (IDs up to ~2³¹)
```java
long modulus = 1_000_000_007L;      // Large prime
long multiplier = 6_364_136_223L;   // Coprime with modulus
IdCodec codec = IdCodecFactory.create(multiplier, modulus);
```

#### For large-scale systems (IDs up to ~2⁶³)
```java
long modulus = 9_223_372_036_854_775_783L;      // Near Long.MAX_VALUE (large prime)
long multiplier = 6_364_136_223_846_793_005L;   // Coprime with modulus
IdCodec codec = IdCodecFactory.create(multiplier, modulus);
```

#### For moderate ID ranges
```java
long modulus = 2_147_483_647L;      // 2³¹ - 1 (prime)
long multiplier = 1_664_525L;       // Coprime with modulus
IdCodec codec = IdCodecFactory.create(multiplier, modulus);
```

### Practical Guidance

- **Choose `modulus`**: Pick a value larger than your maximum expected ID. Prime numbers work well since they're coprime with most multipliers.
  - Common choices: `1_000_000_007`, `2_147_483_647` (2³¹ - 1), `9_223_372_036_854_775_783` (near Long.MAX_VALUE)
  
- **Choose `multiplier`**: Pick any value coprime to the modulus. Odd multipliers work well when modulus is even; any multiplier works with prime moduli.
  - Avoid: `1` (trivial permutation), small values (weak obfuscation), factors of modulus
  - Good: Large, seemingly random odd numbers
  
- **Treat as configuration**: `multiplier` and `modulus` are not cryptographic secrets—they're configuration. Don't hide them from application code.

- **Test coprimality**: The factory validates coprimality automatically and throws an exception if `gcd(multiplier, modulus) ≠ 1`.

## Thread Safety

`EeaIdCodec` is immutable and thread safe. You can share a single instance across threads.

## Build

```bash
mvn test
```

For release signing (Maven Central), activate the `release` profile.

```bash
mvn -P release verify
```

## License

MIT
