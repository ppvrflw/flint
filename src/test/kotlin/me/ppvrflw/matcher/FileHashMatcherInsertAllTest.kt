package me.ppvrflw.matcher

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import me.ppvrflw.record.FileHashRecord

class FileHashMatcherInsertAllTest :
    FunSpec({
      context("insertAll with typed keys") {
        test("inserts multiple entries") {
          val matcher =
              FileHashMatcher<String>().apply {
                insertAll(
                    listOf(
                        FileHashRecord.from("abc123") to "A",
                        FileHashRecord.from("def456") to "B",
                    )
                )
              }

          matcher.match(FileHashRecord.from("abc123")) shouldBe listOf("A")
          matcher.match(FileHashRecord.from("def456")) shouldBe listOf("B")
        }

        test("empty iterable is a no-op") {
          val matcher =
              FileHashMatcher<String>().apply {
                insertAll(emptyList<Pair<FileHashRecord, String>>())
              }

          matcher.match(FileHashRecord.from("abc123")) shouldBe emptyList()
        }

        test("duplicate values are deduplicated") {
          val matcher =
              FileHashMatcher<String>().apply {
                insertAll(
                    listOf(
                        FileHashRecord.from("abc123") to "A",
                        FileHashRecord.from("abc123") to "A",
                    )
                )
              }

          matcher.match(FileHashRecord.from("abc123")) shouldBe listOf("A")
        }
      }

      context("insertAll with raw string keys") {
        test("parses and inserts multiple entries") {
          val matcher =
              FileHashMatcher<String>().apply {
                insertAllRaw(listOf("abc123" to "A", "def456" to "B"))
              }

          matcher.match(FileHashRecord.from("abc123")) shouldBe listOf("A")
          matcher.match(FileHashRecord.from("def456")) shouldBe listOf("B")
        }

        test("empty iterable is a no-op") {
          val matcher =
              FileHashMatcher<String>().apply { insertAllRaw(emptyList<Pair<String, String>>()) }

          matcher.match(FileHashRecord.from("abc123")) shouldBe emptyList()
        }

        test("normalizes case during bulk insert") {
          val matcher = FileHashMatcher<String>().apply { insertAllRaw(listOf("ABC123" to "A")) }

          matcher.match(FileHashRecord.from("abc123")) shouldBe listOf("A")
        }

        test("multiple values on the same hash") {
          val matcher =
              FileHashMatcher<String>().apply {
                insertAllRaw(listOf("abc123" to "A", "abc123" to "B"))
              }

          matcher.match(FileHashRecord.from("abc123")) shouldBe listOf("A", "B")
        }
      }

      context("insertAll with sequence") {
        test("parses and inserts from sequence") {
          val matcher =
              FileHashMatcher<String>().apply {
                insertAllRaw(sequenceOf("abc123" to "A", "def456" to "B"))
              }

          matcher.match(FileHashRecord.from("abc123")) shouldBe listOf("A")
          matcher.match(FileHashRecord.from("def456")) shouldBe listOf("B")
        }

        test("empty sequence is a no-op") {
          val matcher =
              FileHashMatcher<String>().apply {
                insertAllRaw(emptySequence<Pair<String, String>>())
              }

          matcher.match(FileHashRecord.from("abc123")) shouldBe emptyList()
        }

        test("sequence is consumed lazily") {
          var count = 0
          val sequence = sequenceOf("abc123" to "A", "def456" to "B").onEach { count++ }

          val matcher = FileHashMatcher<String>().apply { insertAllRaw(sequence) }

          count shouldBe 2
          matcher.match(FileHashRecord.from("abc123")) shouldBe listOf("A")
        }
      }
    })
