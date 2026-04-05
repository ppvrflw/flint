# Flint

A fast, lightweight, and efficient Indicator of Compromise (IoC) matching library that sparks the detection.

## Types of indicators

1. Domain names
2. File hashes
3. IP addresses (IPv4 and IPv6)
4. more to come

## Usage

### Import the library

#### Maven
```maven
<dependency>
    <groupId>me.ppvrflw</groupId>
    <artifactId>flint</artifactId>
    <version>0.1.0</version>
</dependency>
```

#### Gradle
```gradle
implementation 'me.ppvrflw:flint:0.1.0'
```

### Create an matcher

#### Kotlin
```kotlin
import me.ppvrflw.DomainNameRecord
import me.ppvrflw.matcher.DomainNameMatcher

fun main() {
  var matcher = DomainNameMatcher<String>()

  // insert a domain into the matcher
  matcher.insert(DomainNameRecord.from("google.com"), "google.com")

  // match a domain
  var matches = matcher.match(DomainNameRecord.from("google.com"))
}
```

#### Java
```java
import me.ppvrflw.DomainNameRecord;
import me.ppvrflw.matcher.DomainNameMatcher;

class Main {
    static void main(String[] args) {
        DomainNameMatcher<String> matcher = new DomainNameMatcher<>();

        // insert a domain into the matcher
        matcher.insert(DomainNameRecord.from("google.com"), "google.com");

        // match a domain
        List<String> matches = matcher.match(DomainNameRecord.from("google.com"));
    }
}
```

## Benchmarking

Benchmark settings:

- JDK 24
- OS: macOS
- CPU: Apple M3
- Memory: 16GB

Benchmark results:

```gradle
Benchmark                          Mode  Cnt         Score        Error  Units
DomainNameMatcherBenchmark.match  thrpt   10   7593212.587 ± 248888.412  ops/s
FileHashMatcherBenchmark.match    thrpt   10  21831043.884 ±  36249.227  ops/s
Ipv4MatcherBenchmark.match        thrpt   10   8803675.989 ±  21562.781  ops/s
Ipv6MatcherBenchmark.match        thrpt   10   5277982.925 ±  10273.966  ops/s
```
