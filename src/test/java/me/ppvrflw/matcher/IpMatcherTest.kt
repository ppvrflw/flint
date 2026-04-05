package me.ppvrflw.matcher

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withTests
import io.kotest.matchers.shouldBe
import me.ppvrflw.IpAddressRecord

class IpMatcherTest :
    FunSpec({
      data class Case(val query: String, val expected: List<String>)

      context("IPv4 prefix matching") {
        val matcher =
            IpMatcher<String>().apply {
              insert(IpAddressRecord.from("192.168.0.0/16"), "/16")
              insert(IpAddressRecord.from("192.168.1.0/24"), "/24")
              insert(IpAddressRecord.from("192.168.1.100/32"), "/32")
            }

        withTests(
            nameFn = { it.query },
            Case("192.168.1.100", listOf("/16", "/24", "/32")),
            Case("192.168.1.200", listOf("/16", "/24")),
            Case("192.168.2.1", listOf("/16")),
            Case("10.0.0.1", emptyList()),
        ) { (query, expected) ->
          matcher.match(IpAddressRecord.from(query)) shouldBe expected
        }
      }

      context("IPv6 prefix matching") {
        val matcher =
            IpMatcher<String>().apply {
              insert(IpAddressRecord.from("2001:db8::/32"), "/32")
              insert(IpAddressRecord.from("2001:db8:1::/48"), "/48")
              insert(IpAddressRecord.from("2001:db8:1::1/128"), "/128")
            }

        withTests(
            nameFn = { it.query },
            Case("2001:db8:1::1", listOf("/32", "/48", "/128")),
            Case("2001:db8:1::2", listOf("/32", "/48")),
            Case("2001:db8:2::1", listOf("/32")),
            Case("2002:db8::1", emptyList()),
        ) { (query, expected) ->
          matcher.match(IpAddressRecord.from(query)) shouldBe expected
        }
      }

      context("IPv4 and IPv6 are isolated") {
        val matcher =
            IpMatcher<String>().apply {
              insert(IpAddressRecord.from("192.168.1.0/24"), "v4")
              insert(IpAddressRecord.from("2001:db8::/32"), "v6")
            }

        withTests(
            nameFn = { it.query },
            Case("192.168.1.1", listOf("v4")),
            Case("2001:db8::1", listOf("v6")),
        ) { (query, expected) ->
          matcher.match(IpAddressRecord.from(query)) shouldBe expected
        }
      }

      test("multiple values on the same prefix") {
        val matcher =
            IpMatcher<String>().apply {
              insert(IpAddressRecord.from("10.0.0.0/8"), "A")
              insert(IpAddressRecord.from("10.0.0.0/8"), "B")
            }

        matcher.match(IpAddressRecord.from("10.1.2.3")) shouldBe listOf("A", "B")
      }

      test("default route matches everything") {
        val matcher =
            IpMatcher<String>().apply {
              insert(IpAddressRecord.from("0.0.0.0/0"), "default")
            }

        matcher.match(IpAddressRecord.from("1.2.3.4")) shouldBe listOf("default")
        matcher.match(IpAddressRecord.from("255.255.255.255")) shouldBe listOf("default")
      }

      test("host bits are ignored on insert") {
        val matcher =
            IpMatcher<String>().apply {
              insert(IpAddressRecord.from("192.168.1.99/24"), "A")
            }

        matcher.match(IpAddressRecord.from("192.168.1.1")) shouldBe listOf("A")
        matcher.match(IpAddressRecord.from("192.168.1.200")) shouldBe listOf("A")
      }

      test("remove exact prefix value") {
        val matcher =
            IpMatcher<String>().apply {
              insert(IpAddressRecord.from("10.0.0.0/8"), "A")
              remove(IpAddressRecord.from("10.0.0.0/8"), "A")
            }

        matcher.match(IpAddressRecord.from("10.1.2.3")) shouldBe emptyList()
      }

      test("remove only affects targeted value") {
        val matcher =
            IpMatcher<String>().apply {
              insert(IpAddressRecord.from("10.0.0.0/8"), "A")
              insert(IpAddressRecord.from("10.0.0.0/8"), "B")
              remove(IpAddressRecord.from("10.0.0.0/8"), "A")
            }

        matcher.match(IpAddressRecord.from("10.1.2.3")) shouldBe listOf("B")
      }

      test("remove does not affect parent prefix") {
        val matcher =
            IpMatcher<String>().apply {
              insert(IpAddressRecord.from("10.0.0.0/8"), "parent")
              insert(IpAddressRecord.from("10.1.0.0/16"), "child")
              remove(IpAddressRecord.from("10.1.0.0/16"), "child")
            }

        matcher.match(IpAddressRecord.from("10.1.2.3")) shouldBe listOf("parent")
      }

      test("remove does not affect child prefix") {
        val matcher =
            IpMatcher<String>().apply {
              insert(IpAddressRecord.from("10.0.0.0/8"), "parent")
              insert(IpAddressRecord.from("10.1.0.0/16"), "child")
              remove(IpAddressRecord.from("10.0.0.0/8"), "parent")
            }

        matcher.match(IpAddressRecord.from("10.1.2.3")) shouldBe listOf("child")
      }

      test("remove nonexistent value is a no-op") {
        val matcher =
            IpMatcher<String>().apply {
              insert(IpAddressRecord.from("10.0.0.0/8"), "A")
              remove(IpAddressRecord.from("10.0.0.0/8"), "B")
            }

        matcher.match(IpAddressRecord.from("10.1.2.3")) shouldBe listOf("A")
      }

      test("remove from nonexistent prefix is a no-op") {
        val matcher =
            IpMatcher<String>().apply {
              insert(IpAddressRecord.from("10.0.0.0/8"), "A")
              remove(IpAddressRecord.from("172.16.0.0/12"), "A")
            }

        matcher.match(IpAddressRecord.from("10.1.2.3")) shouldBe listOf("A")
      }

      test("no match returns empty list") {
        val matcher = IpMatcher<String>()

        matcher.match(IpAddressRecord.from("10.0.0.1")) shouldBe emptyList()
      }
    })
