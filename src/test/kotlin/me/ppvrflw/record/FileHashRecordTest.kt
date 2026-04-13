package me.ppvrflw.record

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import me.ppvrflw.matcher.FileHashMatcher

class FileHashRecordTest :
    FunSpec({
      context("parse file hashes") {
        test("creates a record from a valid hex string") {
          val record = FileHashRecord.from("abc123")

          record.hash shouldBe "abc123"
        }

        test("normalizes uppercase to lowercase") {
          val record = FileHashRecord.from("ABC123")

          record.hash shouldBe "abc123"
        }

        test("creates a record from a realistic SHA-256 hash") {
          val record =
              FileHashRecord.from(
                  "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
              )

          record.hash shouldBe "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        }
      }

      context("reject invalid hashes") {
        test("rejects blank hash") {
          shouldThrow<IllegalArgumentException> { FileHashRecord.from("   ") }.message shouldBe
              "hash can't be empty"
        }

        test("rejects empty hash") {
          shouldThrow<IllegalArgumentException> { FileHashRecord.from("") }.message shouldBe
              "hash can't be empty"
        }

        test("rejects non-hexadecimal characters") {
          shouldThrow<IllegalArgumentException> { FileHashRecord.from("xyz123") }.message shouldBe
              "hash must be hexadecimal"
        }

        test("rejects hash with special characters") {
          shouldThrow<IllegalArgumentException> { FileHashRecord.from("abc-123") }.message shouldBe
              "hash must be hexadecimal"
        }

        test("rejects hash with whitespace") {
          shouldThrow<IllegalArgumentException> { FileHashRecord.from("abc 123") }
        }
      }

      context("fromTrusted") {
        test("creates a record without validation") {
          val record = FileHashRecord.fromTrusted("abc123")

          record.hash shouldBe "abc123"
        }

        test("does not normalize to lowercase") {
          val record = FileHashRecord.fromTrusted("ABC123")

          record.hash shouldBe "ABC123"
        }

        test("does not reject non-hexadecimal characters") {
          val record = FileHashRecord.fromTrusted("xyz-!@#")

          record.hash shouldBe "xyz-!@#"
        }
      }

      context("match delegation") {
        test("match delegates to FileHashMatcher") {
          val matcher =
              FileHashMatcher<String>().apply { insert(FileHashRecord.from("abc123"), "A") }

          val record = FileHashRecord.from("abc123")

          record.match(matcher) shouldBe listOf("A")
        }

        test("match returns empty for unregistered hash") {
          val matcher =
              FileHashMatcher<String>().apply { insert(FileHashRecord.from("abc123"), "A") }

          FileHashRecord.from("def456").match(matcher) shouldBe emptyList()
        }
      }
    })
