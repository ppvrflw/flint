package me.ppvrflw.matcher

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldBe
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import me.ppvrflw.DomainNameRecord
import me.ppvrflw.FileHashRecord
import me.ppvrflw.IpAddressRecord

class SynchronizedMatcherTest :
    FunSpec({
      test("delegates insert and match to underlying matcher") {
        val matcher = SynchronizedMatcher(DomainNameMatcher<String>())
        matcher.insert(DomainNameRecord.from("example.com"), "A")

        matcher.match(DomainNameRecord.from("example.com")) shouldBe listOf("A")
      }

      test("delegates remove to underlying matcher") {
        val matcher = SynchronizedMatcher(FileHashMatcher<String>())
        matcher.insert(FileHashRecord.from("abc123"), "A")
        matcher.remove(FileHashRecord.from("abc123"), "A")

        matcher.match(FileHashRecord.from("abc123")) shouldBe emptyList()
      }

      test("concurrent inserts and matches do not lose data") {
        val matcher = SynchronizedMatcher(IpMatcher<String>())
        val threads =
            (1..100).map { i ->
              Thread {
                val cidr = "10.0.${i % 256}.0/24"
                matcher.insert(IpAddressRecord.from(cidr), "val-$i")
              }
            }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        matcher.match(IpAddressRecord.from("10.0.1.1")) shouldBe listOf("val-1")
        matcher.match(IpAddressRecord.from("10.0.100.1")) shouldBe listOf("val-100")
      }

      test("concurrent inserts and removes are consistent") {
        val matcher = SynchronizedMatcher(DomainNameMatcher<String>())
        matcher.insert(DomainNameRecord.from("example.com"), "A")

        val inserters =
            (1..50).map { i ->
              Thread { matcher.insert(DomainNameRecord.from("example.com"), "val-$i") }
            }

        val removers =
            (1..50).map { i ->
              Thread { matcher.remove(DomainNameRecord.from("example.com"), "val-$i") }
            }

        (inserters + removers).shuffled().forEach { it.start() }
        (inserters + removers).forEach { it.join() }

        val results = matcher.match(DomainNameRecord.from("example.com"))
        // "A" was never removed, so it must still be present
        results.contains("A") shouldBe true
      }

      test("insertAll adds multiple entries atomically") {
        val matcher = SynchronizedMatcher(DomainNameMatcher<String>())
        matcher.insertAll(
            listOf(
                DomainNameRecord.from("example.com") to "A",
                DomainNameRecord.from("example.org") to "B",
                DomainNameRecord.from("api.example.com") to "C",
            )
        )

        matcher.match(DomainNameRecord.from("example.com")) shouldBe listOf("A")
        matcher.match(DomainNameRecord.from("example.org")) shouldBe listOf("B")
        matcher.match(DomainNameRecord.from("api.example.com")) shouldBe listOf("A", "C")
      }

      test("removeAll removes multiple entries atomically") {
        val matcher = SynchronizedMatcher(DomainNameMatcher<String>())
        matcher.insertAll(
            listOf(
                DomainNameRecord.from("example.com") to "A",
                DomainNameRecord.from("example.org") to "B",
                DomainNameRecord.from("example.net") to "C",
            )
        )
        matcher.removeAll(
            listOf(
                DomainNameRecord.from("example.com") to "A",
                DomainNameRecord.from("example.org") to "B",
            )
        )

        matcher.match(DomainNameRecord.from("example.com")) shouldBe emptyList()
        matcher.match(DomainNameRecord.from("example.org")) shouldBe emptyList()
        matcher.match(DomainNameRecord.from("example.net")) shouldBe listOf("C")
      }

      test("concurrent writer and reader produce no undefined behavior") {
        val matcher = SynchronizedMatcher(IpMatcher<String>())
        val iterations = 10_000
        val running = AtomicBoolean(true)
        val failure = AtomicReference<Throwable?>(null)

        // Reader: continuously matches and verifies results are valid
        val reader = Thread {
          try {
            while (running.get()) {
              val results = matcher.match(IpAddressRecord.from("10.0.0.1"))
              // Results must be either empty (not yet inserted / already removed)
              // or contain exactly "A" (currently inserted)
              results shouldBeIn listOf(emptyList(), listOf("A", "B", "C"))
            }
          } catch (e: Throwable) {
            failure.compareAndSet(null, e)
          }
        }

        // Writer: repeatedly inserts and removes the same entry
        val writer = Thread {
          try {
            repeat(iterations) {
              matcher.insertAll(
                  listOf(
                      IpAddressRecord.from("10.0.0.0/24") to "A",
                      IpAddressRecord.from("10.0.0.0/24") to "B",
                      IpAddressRecord.from("10.0.0.0/24") to "C",
                  )
              )

              matcher.removeAll(
                  listOf(
                      IpAddressRecord.from("10.0.0.0/24") to "A",
                      IpAddressRecord.from("10.0.0.0/24") to "B",
                      IpAddressRecord.from("10.0.0.0/24") to "C",
                  )
              )
            }
          } catch (e: Throwable) {
            failure.compareAndSet(null, e)
          }
        }

        reader.start()
        writer.start()
        writer.join()
        running.set(false)
        reader.join()

        failure.get()?.let { throw it }

        // After all writes complete, the entry should be fully removed
        matcher.match(IpAddressRecord.from("10.0.0.1")) shouldBe emptyList()
      }
    })
