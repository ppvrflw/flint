package me.ppvrflw.record

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import me.ppvrflw.matcher.DomainNameMatcher

class DomainNameRecordTest :
    FunSpec({
      context("parse domain names") {
        test("parses a simple domain") {
          val record = DomainNameRecord.from("example.com")

          record.tld shouldBe "com"
          record.domain shouldBe "example"
          record.subdomainParts shouldBe null
        }

        test("parses a domain with one subdomain") {
          val record = DomainNameRecord.from("api.example.com")

          record.tld shouldBe "com"
          record.domain shouldBe "example"
          record.subdomainParts shouldBe listOf("api")
        }

        test("parses a domain with multiple subdomains") {
          val record = DomainNameRecord.from("v1.api.example.com")

          record.tld shouldBe "com"
          record.domain shouldBe "example"
          record.subdomainParts shouldBe listOf("v1", "api")
        }

        test("normalizes to lowercase") {
          val record = DomainNameRecord.from("API.Example.COM")

          record.tld shouldBe "com"
          record.domain shouldBe "example"
          record.subdomainParts shouldBe listOf("api")
        }
      }

      context("reject invalid hostnames") {
        test("rejects empty hostname") {
          shouldThrow<IllegalArgumentException> { DomainNameRecord.from("") }.message shouldBe
              "hostname cannot be empty"
        }

        test("rejects single-label hostname") {
          shouldThrow<IllegalArgumentException> { DomainNameRecord.from("localhost") }
              .message shouldBe "hostname must have at least domain and tld"
        }

        test("rejects hostname with trailing dot") {
          shouldThrow<IllegalArgumentException> { DomainNameRecord.from("example.com.") }
              .message shouldBe "hostname parts cannot be empty"
        }

        test("rejects hostname with leading dot") {
          shouldThrow<IllegalArgumentException> { DomainNameRecord.from(".example.com") }
              .message shouldBe "hostname parts cannot be empty"
        }

        test("rejects hostname with whitespace") {
          shouldThrow<IllegalArgumentException> { DomainNameRecord.from(" example.com ") }
              .message shouldBe "hostname cannot contain leading or trailing whitespace"
        }
      }

      context("match delegation") {
        test("match delegates to DomainNameMatcher") {
          val matcher =
              DomainNameMatcher<String>().apply {
                insert(DomainNameRecord.from("example.com"), "A")
              }

          val record = DomainNameRecord.from("example.com")

          record.match(matcher) shouldBe listOf("A")
        }

        test("match returns parent values for subdomain query") {
          val matcher =
              DomainNameMatcher<String>().apply {
                insert(DomainNameRecord.from("example.com"), "A")
              }

          DomainNameRecord.from("api.example.com").match(matcher) shouldBe listOf("A")
        }

        test("match returns empty for unregistered domain") {
          val matcher =
              DomainNameMatcher<String>().apply {
                insert(DomainNameRecord.from("example.com"), "A")
              }

          DomainNameRecord.from("other.com").match(matcher) shouldBe emptyList()
        }
      }
    })
