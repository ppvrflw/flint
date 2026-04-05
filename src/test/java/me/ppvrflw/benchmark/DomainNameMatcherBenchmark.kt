package me.ppvrflw.benchmark

import kotlinx.benchmark.*
import me.ppvrflw.DomainNameRecord
import me.ppvrflw.matcher.DomainNameMatcher
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Mode

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@Fork(1)
class DomainNameMatcherBenchmark {
  private lateinit var matcher: DomainNameMatcher<String>
  private lateinit var queries: List<String>
  private var index = 0

  @Setup
  fun setup() {
    matcher =
        DomainNameMatcher<String>().apply {
          resourceLines("domain_blocklist.txt").forEach { insert(DomainNameRecord.from(it), it) }
        }

    queries = resourceLines("domain_traffic.txt")
  }

  @Benchmark
  fun match(blackhole: Blackhole) {
    blackhole.consume(matcher.match(DomainNameRecord.from(queries[index])))
    index = (index + 1) % queries.size
  }
}
