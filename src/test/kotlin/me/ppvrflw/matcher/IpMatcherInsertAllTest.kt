package me.ppvrflw.matcher

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import me.ppvrflw.record.IpAddressRecord

class IpMatcherInsertAllTest :
    FunSpec({
      context("insertAll with typed keys") {
        test("inserts multiple entries") {
          val matcher =
              IpMatcher<String>().apply {
                insertAll(
                    listOf(
                        IpAddressRecord.from("192.168.0.0/16") to "/16",
                        IpAddressRecord.from("10.0.0.0/8") to "/8",
                    )
                )
              }

          matcher.match(IpAddressRecord.from("192.168.1.1")) shouldBe listOf("/16")
          matcher.match(IpAddressRecord.from("10.1.2.3")) shouldBe listOf("/8")
        }

        test("empty iterable is a no-op") {
          val matcher =
              IpMatcher<String>().apply { insertAll(emptyList<Pair<IpAddressRecord, String>>()) }

          matcher.match(IpAddressRecord.from("10.0.0.1")) shouldBe emptyList()
        }

        test("prefix hierarchy works with bulk insert") {
          val matcher =
              IpMatcher<String>().apply {
                insertAll(
                    listOf(
                        IpAddressRecord.from("10.0.0.0/8") to "/8",
                        IpAddressRecord.from("10.1.0.0/16") to "/16",
                        IpAddressRecord.from("10.1.1.0/24") to "/24",
                    )
                )
              }

          matcher.match(IpAddressRecord.from("10.1.1.1")) shouldBe listOf("/8", "/16", "/24")
        }
      }

      context("insertAll with raw string keys") {
        test("parses and inserts multiple entries") {
          val matcher =
              IpMatcher<String>().apply {
                insertAllRaw(listOf("192.168.0.0/16" to "/16", "10.0.0.0/8" to "/8"))
              }

          matcher.match(IpAddressRecord.from("192.168.1.1")) shouldBe listOf("/16")
          matcher.match(IpAddressRecord.from("10.1.2.3")) shouldBe listOf("/8")
        }

        test("empty iterable is a no-op") {
          val matcher =
              IpMatcher<String>().apply { insertAllRaw(emptyList<Pair<String, String>>()) }

          matcher.match(IpAddressRecord.from("10.0.0.1")) shouldBe emptyList()
        }

        test("IPv6 entries are parsed correctly") {
          val matcher =
              IpMatcher<String>().apply {
                insertAllRaw(listOf("2001:db8::/32" to "v6-net", "2001:db8:1::/48" to "v6-sub"))
              }

          matcher.match(IpAddressRecord.from("2001:db8:1::1")) shouldBe listOf("v6-net", "v6-sub")
        }

        test("mixed IPv4 and IPv6 entries") {
          val matcher =
              IpMatcher<String>().apply {
                insertAllRaw(listOf("10.0.0.0/8" to "v4", "2001:db8::/32" to "v6"))
              }

          matcher.match(IpAddressRecord.from("10.1.2.3")) shouldBe listOf("v4")
          matcher.match(IpAddressRecord.from("2001:db8::1")) shouldBe listOf("v6")
        }
      }

      context("insertAll with sequence") {
        test("parses and inserts from sequence") {
          val matcher =
              IpMatcher<String>().apply {
                insertAllRaw(sequenceOf("192.168.0.0/16" to "/16", "10.0.0.0/8" to "/8"))
              }

          matcher.match(IpAddressRecord.from("192.168.1.1")) shouldBe listOf("/16")
          matcher.match(IpAddressRecord.from("10.1.2.3")) shouldBe listOf("/8")
        }

        test("empty sequence is a no-op") {
          val matcher =
              IpMatcher<String>().apply { insertAllRaw(emptySequence<Pair<String, String>>()) }

          matcher.match(IpAddressRecord.from("10.0.0.1")) shouldBe emptyList()
        }

        test("sequence is consumed lazily") {
          var count = 0
          val sequence = sequenceOf("10.0.0.0/8" to "A", "172.16.0.0/12" to "B").onEach { count++ }

          val matcher = IpMatcher<String>().apply { insertAllRaw(sequence) }

          count shouldBe 2
          matcher.match(IpAddressRecord.from("10.1.2.3")) shouldBe listOf("A")
        }
      }
    })
