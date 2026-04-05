package me.ppvrflw.benchmark

import me.ppvrflw.FileHashRecord
import me.ppvrflw.matcher.FileHashMatcher
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@Fork(1)
open class FileHashMatcherBenchmark {
  private lateinit var matcher: FileHashMatcher<String>
  private var queries: MutableList<String> = mutableListOf()
  private var index = 0

  @Setup
  fun setup() {
    matcher =
        FileHashMatcher<String>().apply {
          List(5) {
            val hash = randomHash()
            insert(FileHashRecord.from(hash), hash)
          }
        }

    List(100) { queries.add(randomHash()) }
  }

  @Benchmark
  fun match(blackhole: Blackhole) {
    blackhole.consume(matcher.match(FileHashRecord.from(queries[index])))
    index = (index + 1) % queries.size
  }

  fun randomHash(algorithm: String = "SHA-256", numBytes: Int = 64): String {
    val bytes = ByteArray(numBytes)
    SecureRandom().nextBytes(bytes)
    return MessageDigest.getInstance(algorithm).digest(bytes).joinToString("") { "%02x".format(it) }
  }
}
