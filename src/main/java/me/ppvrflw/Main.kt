package me.ppvrflw

import me.ppvrflw.matcher.IpMatcher

fun main() {
  val ipMatcher = IpMatcher<String>()
  ipMatcher.insert(IpAddressRecord.from("2001:db8:c01::1"), "test")

  for(i in 0..1_000_000) {
    ipMatcher.match(IpAddressRecord.from("2001:db8:c01::1"))
  }
}
