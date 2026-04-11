package me.ppvrflw.matcher

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import me.ppvrflw.record.DomainNameRecord

class DomainNameMatcherInsertAllTest :
    FunSpec({
      context("insertAll with typed keys") {
        test("inserts multiple entries") {
          val matcher =
              DomainNameMatcher<String>().apply {
                insertAll(
                    listOf(
                        DomainNameRecord.from("example.com") to "A",
                        DomainNameRecord.from("api.example.com") to "B",
                    )
                )
              }

          matcher.match(DomainNameRecord.from("example.com")) shouldBe listOf("A")
          matcher.match(DomainNameRecord.from("api.example.com")) shouldBe listOf("A", "B")
        }

        test("empty iterable is a no-op") {
          val matcher = DomainNameMatcher<String>().apply { insertAll(emptyList()) }

          matcher.match(DomainNameRecord.from("example.com")) shouldBe emptyList()
        }

        test("duplicate values are deduplicated") {
          val matcher =
              DomainNameMatcher<String>().apply {
                insertAll(
                    listOf(
                        DomainNameRecord.from("example.com") to "A",
                        DomainNameRecord.from("example.com") to "A",
                    )
                )
              }

          matcher.match(DomainNameRecord.from("example.com")) shouldBe listOf("A")
        }
      }

      context("insertAll with raw string keys") {
        test("parses and inserts multiple entries") {
          val matcher =
              DomainNameMatcher<String>().apply {
                insertAllRaw(listOf("example.com" to "A", "api.example.com" to "B"))
              }

          matcher.match(DomainNameRecord.from("example.com")) shouldBe listOf("A")
          matcher.match(DomainNameRecord.from("api.example.com")) shouldBe listOf("A", "B")
        }

        test("empty iterable is a no-op") {
          val matcher =
              DomainNameMatcher<String>().apply { insertAllRaw(emptyList<Pair<String, String>>()) }

          matcher.match(DomainNameRecord.from("example.com")) shouldBe emptyList()
        }

        test("subdomain hierarchy works with bulk insert") {
          val matcher =
              DomainNameMatcher<String>().apply {
                insertAllRaw(
                    listOf(
                        "example.com" to "root",
                        "api.example.com" to "api",
                        "v1.api.example.com" to "v1",
                    )
                )
              }

          matcher.match(DomainNameRecord.from("v1.api.example.com")) shouldBe
              listOf("root", "api", "v1")
        }
      }

      context("insertAll with sequence") {
        test("parses and inserts from sequence") {
          val matcher =
              DomainNameMatcher<String>().apply {
                insertAllRaw(
                    sequenceOf("example.com" to "A", "other.org" to "B"),
                )
              }

          matcher.match(DomainNameRecord.from("example.com")) shouldBe listOf("A")
          matcher.match(DomainNameRecord.from("other.org")) shouldBe listOf("B")
        }

        test("empty sequence is a no-op") {
          val matcher = DomainNameMatcher<String>().apply { insertAllRaw(emptySequence()) }

          matcher.match(DomainNameRecord.from("example.com")) shouldBe emptyList()
        }

        test("sequence is consumed lazily") {
          var count = 0
          val sequence = sequenceOf("example.com" to "A", "other.org" to "B").onEach { count++ }

          val matcher = DomainNameMatcher<String>().apply { insertAllRaw(sequence) }

          count shouldBe 2
          matcher.match(DomainNameRecord.from("example.com")) shouldBe listOf("A")
        }
      }
    })
