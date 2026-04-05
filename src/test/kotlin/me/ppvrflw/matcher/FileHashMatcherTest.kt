package me.ppvrflw.matcher

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import me.ppvrflw.FileHashRecord

class FileHashMatcherTest :
    FunSpec({
      test("match exact hash") {
        val matcher =
            FileHashMatcher<String>().apply {
              insert(FileHashRecord.from("abc123"), "A")
            }

        matcher.match(FileHashRecord.from("abc123")) shouldBe listOf("A")
      }

      test("no match for unknown hash") {
        val matcher =
            FileHashMatcher<String>().apply {
              insert(FileHashRecord.from("abc123"), "A")
            }

        matcher.match(FileHashRecord.from("def456")) shouldBe emptyList()
      }

      test("multiple values on the same hash") {
        val matcher =
            FileHashMatcher<String>().apply {
              insert(FileHashRecord.from("abc123"), "A")
              insert(FileHashRecord.from("abc123"), "B")
            }

        matcher.match(FileHashRecord.from("abc123")) shouldBe listOf("A", "B")
      }

      test("duplicate insert is idempotent") {
        val matcher =
            FileHashMatcher<String>().apply {
              insert(FileHashRecord.from("abc123"), "A")
              insert(FileHashRecord.from("abc123"), "A")
            }

        matcher.match(FileHashRecord.from("abc123")) shouldBe listOf("A")
      }

      test("different hashes are independent") {
        val matcher =
            FileHashMatcher<String>().apply {
              insert(FileHashRecord.from("abc123"), "A")
              insert(FileHashRecord.from("def456"), "B")
            }

        matcher.match(FileHashRecord.from("abc123")) shouldBe listOf("A")
        matcher.match(FileHashRecord.from("def456")) shouldBe listOf("B")
      }

      test("case insensitive via convert") {
        val matcher =
            FileHashMatcher<String>().apply {
              insert(FileHashRecord.from("ABC123"), "A")
            }

        matcher.match(FileHashRecord.from("abc123")) shouldBe listOf("A")
      }

      test("remove value") {
        val matcher =
            FileHashMatcher<String>().apply {
              insert(FileHashRecord.from("abc123"), "A")
              remove(FileHashRecord.from("abc123"), "A")
            }

        matcher.match(FileHashRecord.from("abc123")) shouldBe emptyList()
      }

      test("remove only affects targeted value") {
        val matcher =
            FileHashMatcher<String>().apply {
              insert(FileHashRecord.from("abc123"), "A")
              insert(FileHashRecord.from("abc123"), "B")
              remove(FileHashRecord.from("abc123"), "A")
            }

        matcher.match(FileHashRecord.from("abc123")) shouldBe listOf("B")
      }

      test("remove nonexistent value is a no-op") {
        val matcher =
            FileHashMatcher<String>().apply {
              insert(FileHashRecord.from("abc123"), "A")
              remove(FileHashRecord.from("abc123"), "B")
            }

        matcher.match(FileHashRecord.from("abc123")) shouldBe listOf("A")
      }

      test("remove from nonexistent hash is a no-op") {
        val matcher =
            FileHashMatcher<String>().apply {
              insert(FileHashRecord.from("abc123"), "A")
              remove(FileHashRecord.from("def456"), "A")
            }

        matcher.match(FileHashRecord.from("abc123")) shouldBe listOf("A")
      }
    })