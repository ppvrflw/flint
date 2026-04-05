package me.ppvrflw

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class IpAddressRecordTest :
    FunSpec({
      context("convert IPv4 addresses") {
        test("parses a plain IPv4 address with default prefix") {
          val record = IpAddressRecord.from("192.168.1.1")

          record.prefix shouldBe 32
          record.ipNumberHigh shouldBe 0u
          record.ipNumberLow shouldBe 0xC0A80101u
        }

        test("parses an IPv4 CIDR address") {
          val record = IpAddressRecord.from("10.0.0.1/24")

          record.prefix shouldBe 24
          record.ipNumberHigh shouldBe 0u
          record.ipNumberLow shouldBe 0x0A000001u
        }

        test("rejects invalid CIDR notation with more than one slash") {
          shouldThrow<IllegalArgumentException> { IpAddressRecord.from("10.0.0.1/24/extra") }
        }

        test("rejects invalid IPv4 octet count") {
          shouldThrow<IllegalArgumentException> { IpAddressRecord.from("10.0.0/24") }
        }

        test("rejects invalid IPv4 octet values") {
          shouldThrow<IllegalArgumentException> { IpAddressRecord.from("256.0.0.1") }
        }

        test("rejects invalid prefix length") {
          shouldThrow<IllegalArgumentException> { IpAddressRecord.from("10.0.0.1/33") }
        }
      }

      context("IPv4 binary formatting helpers") {
        test("prints a 32-bit binary string with spaces every 8 bits") {
          val record = IpAddressRecord.from("192.168.1.1/24")
          val binary = record.ipNumberLow.toString(2).padStart(64, '0').chunked(8).joinToString(" ")

          binary shouldBe "00000000 00000000 00000000 00000000 11000000 10101000 00000001 00000001"
        }
      }

      context("convert IPv6 addresses") {
        test("parses a plain IPv6 address with default prefix") {
          val record = IpAddressRecord.from("3e59:a0cf:8431:9b75:1306:3797:e46e:deab")

          record.prefix shouldBe 128
          record.ipNumberHigh shouldBe 0x3e59a0cf84319b75u
          record.ipNumberLow shouldBe 0x13063797e46edeabu
        }

        test("parses a compressed IPv6 address") {
          val record = IpAddressRecord.from("2001:db8::1")

          record.prefix shouldBe 128
          record.ipNumberHigh shouldBe 0x20010db800000000u
          record.ipNumberLow shouldBe 0x0000000000000001u
        }

        test("parses an IPv6 CIDR address") {
          val record = IpAddressRecord.from("fe80::/10")

          record.prefix shouldBe 10
          record.ipNumberHigh shouldBe 0xfe80000000000000u
          record.ipNumberLow shouldBe 0u
        }

        test("parses an IPv6 address with a zone id") {
          val record = IpAddressRecord.from("fe80::1%eth0")

          record.prefix shouldBe 128
          record.ipNumberHigh shouldBe 0xfe80000000000000u
          record.ipNumberLow shouldBe 0x0000000000000001u
        }

        test("parses an IPv4-mapped IPv6 address") {
          val record = IpAddressRecord.from("::ffff:192.0.2.1")

          record.prefix shouldBe 128
          record.ipNumberHigh shouldBe 0x0000000000000000u
          record.ipNumberLow shouldBe 0x00000000c0000201u
        }

        test("parses an IPv4-mapped IPv6 address with hex notation") {
          val record = IpAddressRecord.from("::ffff:c000:201")

          record.prefix shouldBe 128
          record.ipNumberHigh shouldBe 0x0000000000000000u
          record.ipNumberLow shouldBe 0x00000000c0000201u
        }

        test("rejects invalid IPv6 prefix length") {
          shouldThrow<IllegalArgumentException> { IpAddressRecord.from("2001:db8::1/129") }
        }

        test("rejects invalid IPv6 address") {
          shouldThrow<IllegalArgumentException> { IpAddressRecord.from("2001:db8:::1") }
        }
      }

      context("IPv6 binary formatting helpers") {
        test("splits the IPv6 value into two 64-bit parts correctly") {
          val record = IpAddressRecord.from("3e59:a0cf:8431:9b75:1306:3797:e46e:deab")

          record.ipNumberHigh.toString(16) shouldBe "3e59a0cf84319b75"
          record.ipNumberLow.toString(16) shouldBe "13063797e46edeab"
        }
      }
    })
