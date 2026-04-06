package me.ppvrflw.matcher

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withTests
import io.kotest.matchers.shouldBe
import me.ppvrflw.record.DomainNameRecord

class DomainNameMatcherTest :
    FunSpec({
      data class Case(val query: String, val expected: List<String>)

      context("single domain and subdomain hierarchy") {
        val matcher =
            DomainNameMatcher<String>().apply {
              insert(DomainNameRecord.from("example.com"), "A")
              insert(DomainNameRecord.from("api.example.com"), "B")
              insert(DomainNameRecord.from("v1.api.example.com"), "C")
            }

        withTests(
            nameFn = { it.query },
            Case("example.com", listOf("A")),
            Case("api.example.com", listOf("A", "B")),
            Case("v1.api.example.com", listOf("A", "B", "C")),
            Case("unknown.com", emptyList()),
            Case("example.org", emptyList()),
            Case("www.example.com", listOf("A")),
        ) { (query, expected) ->
          matcher.match(DomainNameRecord.from(query)) shouldBe expected
        }
      }

      context("sibling subdomains are independent") {
        val matcher =
            DomainNameMatcher<String>().apply {
              insert(DomainNameRecord.from("api.example.com"), "A")
              insert(DomainNameRecord.from("rss.example.com"), "B")
            }

        withTests(
            nameFn = { it.query },
            Case("api.example.com", listOf("A")),
            Case("rss.example.com", listOf("B")),
        ) { (query, expected) ->
          matcher.match(DomainNameRecord.from(query)) shouldBe expected
        }
      }

      context("multiple values on the same domain") {
        val matcher =
            DomainNameMatcher<String>().apply {
              insert(DomainNameRecord.from("api.example.com"), "A")
              insert(DomainNameRecord.from("api.example.com"), "B")
            }

        test("returns all values") {
          matcher.match(DomainNameRecord.from("api.example.com")) shouldBe listOf("A", "B")
        }
      }

      test("duplicate insert is idempotent") {
        val matcher =
            DomainNameMatcher<String>().apply {
              insert(DomainNameRecord.from("api.example.com"), "A")
              insert(DomainNameRecord.from("api.example.com"), "A")
            }

        matcher.match(DomainNameRecord.from("api.example.com")) shouldBe listOf("A")
      }

      test("subdomain does not match parent query") {
        val matcher =
            DomainNameMatcher<String>().apply {
              insert(DomainNameRecord.from("api.example.com"), "A")
            }

        matcher.match(DomainNameRecord.from("example.com")) shouldBe emptyList()
      }

      test("remove exact domain value") {
        val matcher =
            DomainNameMatcher<String>().apply {
              insert(DomainNameRecord.from("example.com"), "A")
              remove(DomainNameRecord.from("example.com"), "A")
            }

        matcher.match(DomainNameRecord.from("example.com")) shouldBe emptyList()
      }

      test("remove subdomain value") {
        val matcher =
            DomainNameMatcher<String>().apply {
              insert(DomainNameRecord.from("api.example.com"), "A")
              remove(DomainNameRecord.from("api.example.com"), "A")
            }

        matcher.match(DomainNameRecord.from("api.example.com")) shouldBe emptyList()
      }

      test("remove only affects the targeted value") {
        val matcher =
            DomainNameMatcher<String>().apply {
              insert(DomainNameRecord.from("api.example.com"), "A")
              insert(DomainNameRecord.from("api.example.com"), "B")
              remove(DomainNameRecord.from("api.example.com"), "A")
            }

        matcher.match(DomainNameRecord.from("api.example.com")) shouldBe listOf("B")
      }

      test("remove does not affect parent values") {
        val matcher =
            DomainNameMatcher<String>().apply {
              insert(DomainNameRecord.from("example.com"), "A")
              insert(DomainNameRecord.from("api.example.com"), "B")
              remove(DomainNameRecord.from("api.example.com"), "B")
            }

        matcher.match(DomainNameRecord.from("api.example.com")) shouldBe listOf("A")
        matcher.match(DomainNameRecord.from("example.com")) shouldBe listOf("A")
      }

      test("remove does not affect child values") {
        val matcher =
            DomainNameMatcher<String>().apply {
              insert(DomainNameRecord.from("example.com"), "A")
              insert(DomainNameRecord.from("api.example.com"), "B")
              remove(DomainNameRecord.from("example.com"), "A")
            }

        matcher.match(DomainNameRecord.from("api.example.com")) shouldBe listOf("B")
        matcher.match(DomainNameRecord.from("example.com")) shouldBe emptyList()
      }

      test("remove nonexistent value is a no-op") {
        val matcher =
            DomainNameMatcher<String>().apply {
              insert(DomainNameRecord.from("example.com"), "A")
              remove(DomainNameRecord.from("example.com"), "B")
            }

        matcher.match(DomainNameRecord.from("example.com")) shouldBe listOf("A")
      }

      test("remove from nonexistent domain is a no-op") {
        val matcher =
            DomainNameMatcher<String>().apply {
              insert(DomainNameRecord.from("example.com"), "A")
              remove(DomainNameRecord.from("unknown.com"), "A")
            }

        matcher.match(DomainNameRecord.from("example.com")) shouldBe listOf("A")
      }

      context("different TLDs are independent") {
        val matcher =
            DomainNameMatcher<String>().apply {
              insert(DomainNameRecord.from("example.com"), "A")
              insert(DomainNameRecord.from("example.org"), "B")
            }

        withTests(
            nameFn = { it.query },
            Case("example.com", listOf("A")),
            Case("example.org", listOf("B")),
        ) { (query, expected) ->
          matcher.match(DomainNameRecord.from(query)) shouldBe expected
        }
      }
    })
