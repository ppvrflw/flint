package me.ppvrflw.record

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class EmailRecordTest :
    FunSpec({
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

      test("rejects empty email") {
        shouldThrow<IllegalArgumentException> { EmailRecord.from("") }
      }

      test("rejects email without @ symbol") {
        shouldThrow<IllegalArgumentException> { EmailRecord.from("userexample.com") }
      }

      test("rejects email with multiple @ symbols") {
        shouldThrow<IllegalArgumentException> { EmailRecord.from("user@name@example.com") }
      }

      test("rejects email with empty local part") {
        shouldThrow<IllegalArgumentException> { EmailRecord.from("@example.com") }
      }

      test("rejects email with empty hostname") {
        shouldThrow<IllegalArgumentException> { EmailRecord.from("user@") }
      }
    })