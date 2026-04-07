package me.ppvrflw.record

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import me.ppvrflw.matcher.DomainNameMatcher

class EmailRecordTest :
    FunSpec({
      context("convert email addresses") {
        test("parses a valid email") {
          val record = EmailRecord.from("user@example.com")

          record.localPart shouldBe "user"
          record.domainNameRecord.tld shouldBe "com"
          record.domainNameRecord.domain shouldBe "example"
        }

        test("parses email with subdomain") {
          val record = EmailRecord.from("user@mail.example.com")

          record.localPart shouldBe "user"
          record.domainNameRecord.subdomainParts shouldBe listOf("mail")
        }
      }

      context("rejects invalid emails") {
        test("rejects empty email") {
          shouldThrow<IllegalArgumentException> { EmailRecord.from("") }.message shouldBe
              "email cannot be empty"
        }

        test("rejects email without @ symbol") {
          shouldThrow<IllegalArgumentException> { EmailRecord.from("userexample.com") }
              .message shouldBe "email must have exactly one @ symbol"
        }

        test("rejects email with multiple @ symbols") {
          shouldThrow<IllegalArgumentException> { EmailRecord.from("user@name@example.com") }
              .message shouldBe "email must have exactly one @ symbol"
        }

        test("rejects email with empty local part") {
          shouldThrow<IllegalArgumentException> { EmailRecord.from("@example.com") }
              .message shouldBe "local part cannot be empty"
        }

        test("rejects email with empty hostname") {
          shouldThrow<IllegalArgumentException> { EmailRecord.from("user@") }.message shouldBe
              "hostname cannot be empty"
        }
      }

      context("match delegation") {
        test("match delegates to domain part of the email") {
          val matcher =
              DomainNameMatcher<String>().apply {
                insert(DomainNameRecord.from("example.com"), "A")
              }

          val record = EmailRecord.from("user@example.com")

          record.match(matcher) shouldBe listOf("A")
        }

        test("match does not use local part for matching") {
          val matcher =
              DomainNameMatcher<String>().apply {
                insert(DomainNameRecord.from("example.com"), "A")
              }

          EmailRecord.from("alice@example.com").match(matcher) shouldBe listOf("A")
          EmailRecord.from("bob@example.com").match(matcher) shouldBe listOf("A")
        }
      }
    })
