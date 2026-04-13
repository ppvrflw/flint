package me.ppvrflw.record

import java.net.InetAddress
import me.ppvrflw.IpMatchable
import me.ppvrflw.matcher.IpMatcher

/**
 * Represents an IP address or CIDR range using two unsigned 64-bit values.
 *
 * IPv4 addresses are stored in [ipNumberLow], while IPv6 addresses are split across [ipNumberHigh]
 * and [ipNumberLow]. Use the companion object's [from] method to parse IP address strings with
 * validation, or [fromTrusted] to skip validation for performance-critical paths.
 *
 * @property ipNumberHigh High 64 bits of the address.
 * @property ipNumberLow Low 64 bits of the address.
 * @property prefix CIDR prefix length. Defaults to /32 for IPv4 and /128 for IPv6.
 */
@ConsistentCopyVisibility
data class IpAddressRecord
private constructor(
    val ipVersion: IpVersion,
    val ipNumberHigh: ULong,
    val ipNumberLow: ULong,
    val prefix: Int,
) : IpMatchable {
  override fun <V> match(matcher: IpMatcher<V>): List<V> {
    return matcher.match(this)
  }

  companion object : RecordFactory<IpAddressRecord> {
    private const val IPV4_OCTET_COUNT = 4
    private const val MAX_OCTET_VALUE = 255uL

    /**
     * Parses the given string into an [IpAddressRecord].
     *
     * @param raw IP address or CIDR string.
     * @return Parsed [IpAddressRecord].
     * @throws IllegalArgumentException if the input is invalid.
     */
    override fun from(raw: String): IpAddressRecord {
      val (ipString, prefixString) =
          when {
            '/' in raw -> raw.split('/', limit = 2)
            else -> listOf(raw, "")
          }

      require(ipString.isNotEmpty()) { "invalid IP address: $raw" }
      val prefix =
          prefixString
              .takeIf { it.isNotEmpty() }
              ?.let { it.toIntOrNull() ?: throw IllegalArgumentException("invalid prefix: $it") }

      return when {
        ':' in ipString -> parseAsIpv6(ipString, prefix, validate = true)
        else -> parseAsIpv4(ipString, prefix, validate = true)
      }
    }

    /**
     * Parses an IP address or CIDR string into an [IpAddressRecord], skipping validation.
     *
     * Use this when the input is already known to be valid (e.g., from a trusted data source or
     * after prior validation) and performance is critical.
     *
     * @param raw A pre-validated IP address or CIDR string.
     * @return A new [IpAddressRecord] instance.
     */
    override fun fromTrusted(raw: String): IpAddressRecord {
      val (ipString, prefixString) =
          when {
            '/' in raw -> raw.split('/', limit = 2)
            else -> listOf(raw, "")
          }

      val prefix = prefixString.takeIf { it.isNotEmpty() }?.toIntOrNull()

      return when {
        ':' in ipString -> parseAsIpv6(ipString, prefix, validate = false)
        else -> parseAsIpv4(ipString, prefix, validate = false)
      }
    }

    /**
     * Parses an IPv4 address string into an [IpAddressRecord].
     *
     * @param ip IPv4 address in dotted-decimal notation.
     * @param prefix Optional CIDR prefix length.
     * @return Parsed IPv4 [IpAddressRecord].
     * @throws IllegalArgumentException if the IPv4 address or prefix is invalid.
     */
    private fun parseAsIpv4(ip: String, prefix: Int?, validate: Boolean): IpAddressRecord {
      return parseLikeIpv4(IpVersion.IPv4, ip, prefix, validate)
    }

    /**
     * Parses an address string into an [IpAddressRecord] like an IPv4 address. Supports IPv4-mapped
     * IPv6.
     *
     * @param ipVersion The IP version of the address.
     * @param ip IPv4 address in dotted-decimal notation.
     * @param prefix Optional CIDR prefix length.
     * @return Parsed IPv4 [IpAddressRecord].
     * @throws IllegalArgumentException if the IPv4 address or prefix is invalid.
     */
    private fun parseLikeIpv4(
        ipVersion: IpVersion,
        ip: String,
        prefix: Int?,
        validate: Boolean,
    ): IpAddressRecord {
      val octets = ip.split('.').map { it.toULong() }

      if (validate) {
        require(octets.size == IPV4_OCTET_COUNT) { "invalid IPv4 address: $ip" }
        require(octets.all { it <= MAX_OCTET_VALUE }) { "invalid IPv4 address: $ip" }
        require(prefix == null || prefix in 0..ipVersion.maxPrefix) {
          "invalid prefix length: $prefix"
        }
      }

      val ipNumber = octets.fold(0uL) { acc, octet -> (acc shl 8) or octet }

      return IpAddressRecord(
          ipVersion = ipVersion,
          ipNumberHigh = 0uL,
          ipNumberLow = ipNumber,
          prefix = prefix ?: ipVersion.maxPrefix,
      )
    }

    /**
     * Parses an IPv6 address string into an [IpAddressRecord].
     *
     * Supports compressed notation, zone IDs, and IPv4-mapped IPv6 addresses.
     *
     * @param ip IPv6 address string.
     * @param prefix Optional CIDR prefix length.
     * @return Parsed IPv6 [IpAddressRecord], or IPv4 [IpAddressRecord] for IPv4-mapped addresses.
     * @throws IllegalArgumentException if the IPv6 address or prefix is invalid.
     */
    private fun parseAsIpv6(ip: String, prefix: Int?, validate: Boolean): IpAddressRecord {
      val ipVersion = IpVersion.IPv6
      val cleanIp = ip.substringBefore('%')

      val addressBytes =
          runCatching { InetAddress.getByName(cleanIp).address }
              .getOrElse { throw IllegalArgumentException("invalid IPv6 address: $ip") }

      if (addressBytes.size == IPV4_OCTET_COUNT) {
        return parseLikeIpv4(
            ipVersion,
            InetAddress.getByAddress(addressBytes).hostAddress,
            prefix,
            validate,
        )
      }

      if (validate) {
        require(prefix == null || prefix in 0..ipVersion.maxPrefix) {
          "invalid prefix length: $prefix"
        }
      }

      val high = addressBytes.toULong(startIndex = 0)
      val low = addressBytes.toULong(startIndex = 8)

      return IpAddressRecord(
          ipVersion = ipVersion,
          ipNumberHigh = high,
          ipNumberLow = low,
          prefix = prefix ?: ipVersion.maxPrefix,
      )
    }

    /** Converts a byte array to an unsigned long value. */
    private fun ByteArray.toULong(startIndex: Int): ULong =
        (startIndex until startIndex + 8).fold(0uL) { acc, i ->
          (acc shl 8) or this[i].toUByte().toULong()
        }
  }
}
