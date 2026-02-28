# id-obfuscator

[![Maven Central](https://img.shields.io/maven-central/v/io.github.arnabbir/id-obfuscator.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.github.arnabbir/id-obfuscator)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Deterministic, reversible obfuscation for sequential numeric IDs using modular multiplicative inverses.

> This is **not encryption**.  
> It is a fast, one-to-one permutation over the range `[0, modulus)`.

## Official Links

- Maven Central  
  https://central.sonatype.com/artifact/io.github.arnabbir/id-obfuscator

- Maven Search  
  https://search.maven.org/artifact/io.github.arnabbir/id-obfuscator

- OSS Index (Security Report)  
  https://ossindex.sonatype.org/component/pkg:maven/io.github.arnabbir/id-obfuscator@1.0.2

- Source Code  
  https://github.com/ArnabBir/id-obfuscator

# Why id-obfuscator?

Sequential numeric IDs expose:

- Enumeration attack surfaces
- Business growth patterns
- Internal system ordering
- Predictable object discovery

Instead of switching to UUIDs (16 bytes, slower indexing, verbose URLs),  
`id-obfuscator` keeps

- 8-byte `long`
- O(1) transformation
- Zero dependencies
- Deterministic reversibility
- High performance indexing
- Pure arithmetic (no strings, no crypto)

# Installation

## Maven

```xml
<dependency>
  <groupId>io.github.arnabbir</groupId>
  <artifactId>id-obfuscator</artifactId>
  <version>1.0.2</version>
</dependency>
```

## Quick Start

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
Artifacts are
- Signed with GPG
- Published via Sonatype Central Portal
- Synced to Maven Central

## Design Principles

- Immutability 
- Fail-fast validation 
- Pure arithmetic transformation 
- O(1) operations 
- No external runtime dependencies 
- Enterprise grade test coverage

## License
[MIT](https://opensource.org/licenses/MIT)
