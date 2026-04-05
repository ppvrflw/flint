package me.ppvrflw

enum class IpVersion(val maxPrefix: Int) {
  IPv4(maxPrefix = 32),
  IPv6(maxPrefix = 128),
}
