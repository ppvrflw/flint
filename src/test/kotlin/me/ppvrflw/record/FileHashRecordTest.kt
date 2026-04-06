package me.ppvrflw.record

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class FileHashRecordTest :
    FunSpec({
      test("creates a record from a valid hex string") {
        val record = FileHashRecord.from("abc123")

        record.hash shouldBe "abc123"
      }

      test("normalizes uppercase to lowercase") {
        val record = FileHashRecord.from("ABC123")

        record.hash shouldBe "abc123"
      }

      test("rejects blank hash") {
        shouldThrow<IllegalArgumentException> { FileHashRecord.from("   ") }
      }

      test("rejects empty hash") {
        shouldThrow<IllegalArgumentException> { FileHashRecord.from("") }
      }

      test("rejects non-hexadecimal characters") {
        shouldThrow<IllegalArgumentException> { FileHashRecord.from("xyz123") }
      }

      test("rejects hash with special characters") {
        shouldThrow<IllegalArgumentException> { FileHashRecord.from("abc-123") }
      }

      test("rejects hash with uppercase characters") {
        shouldThrow<IllegalArgumentException> { FileHashRecord("ABC12345") }
      }
    })