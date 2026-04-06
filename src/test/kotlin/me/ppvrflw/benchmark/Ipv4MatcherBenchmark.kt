package me.ppvrflw.benchmark

import me.ppvrflw.matcher.IpMatcher
import me.ppvrflw.record.IpAddressRecord
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole

@State(Scope.Benchmark)
@Fork(1)
class Ipv4MatcherBenchmark {
  private lateinit var matcher: IpMatcher<String>
  private lateinit var queries: List<String>
  private var index = 0

  @Setup
  fun setup() {
    matcher =
        IpMatcher<String>().apply {
          resourceLines("ipv4_blocklist.txt").forEach { insert(IpAddressRecord.from(it), it) }
        }

    queries = resourceLines("ipv4_traffic.txt")
  }

  @Benchmark
  fun match(blackhole: Blackhole) {
    blackhole.consume(matcher.match(IpAddressRecord.from(queries[index])))
    index = (index + 1) % queries.size
  }
}
