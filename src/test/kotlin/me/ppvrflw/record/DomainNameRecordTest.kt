package me.ppvrflw.record

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class DomainNameRecordTest :
    FunSpec({
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

      test("rejects empty hostname") {
        shouldThrow<IllegalArgumentException> { DomainNameRecord.from("") }
      }

      test("rejects single-label hostname") {
        shouldThrow<IllegalArgumentException> { DomainNameRecord.from("localhost") }
      }
    })