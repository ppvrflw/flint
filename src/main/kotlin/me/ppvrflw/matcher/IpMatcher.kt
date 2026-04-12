package me.ppvrflw.matcher

import me.ppvrflw.AbstractMatcher
import me.ppvrflw.record.IpAddressRecord
import me.ppvrflw.record.IpVersion

/**
 * A [Matcher] that indexes and matches values by IP address, supporting CIDR prefix matching.
 *
 * Routes IPv4 and IPv6 records to separate [BinaryPrefixTrie] instances. Matching an IP address
 * returns all values registered at that exact address and any covering CIDR prefix (e.g. querying
 * `192.168.1.1` returns values from both `192.168.1.1/32` and `192.168.0.0/16`).
 *
 * @param V the type of value associated with each key
 */
class IpMatcher<V> : AbstractMatcher<IpAddressRecord, V>(IpAddressRecord::from) {

  private val tries = IpVersion.entries.associateWith { BinaryPrefixTrie<V>() }

  override fun insert(key: IpAddressRecord, value: V) {
    trieFor(key).insert(toTokens(key), key.prefix, value)
  }

  override fun remove(key: IpAddressRecord, value: V) {
    trieFor(key).remove(toTokens(key), key.prefix, value)
  }

  override fun match(key: IpAddressRecord): List<V> {
    return trieFor(key).match(toTokens(key), key.prefix)
  }

  private fun trieFor(record: IpAddressRecord) = tries.getValue(record.ipVersion)

  private fun toTokens(record: IpAddressRecord): LongArray =
      if (record.ipVersion == IpVersion.IPv4) longArrayOf((record.ipNumberLow shl 32).toLong())
      else longArrayOf(record.ipNumberHigh.toLong(), record.ipNumberLow.toLong())
}
