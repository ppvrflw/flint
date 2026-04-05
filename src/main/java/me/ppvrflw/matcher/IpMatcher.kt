package me.ppvrflw.matcher

import me.ppvrflw.IpAddressRecord
import me.ppvrflw.IpVersion
import me.ppvrflw.Matcher
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Thread-safe IP address matcher that routes IPv4 and IPv6 records to separate binary prefix tries.
 */
class IpMatcher<V> : Matcher<IpAddressRecord, V> {

  private val tries = IpVersion.entries.associateWith { BinaryPrefixTrie<V>() }
  private val lock = ReentrantReadWriteLock()

  override fun insert(key: IpAddressRecord, value: V) {
    lock.write { trieFor(key).insert(toTokens(key), key.prefix, value) }
  }

  override fun remove(key: IpAddressRecord, value: V) {
    lock.write { trieFor(key).remove(toTokens(key), key.prefix, value) }
  }

  override fun match(key: IpAddressRecord): List<V> {
    lock.read { return trieFor(key).match(toTokens(key), key.prefix) }
  }

  private fun trieFor(record: IpAddressRecord) = tries.getValue(record.ipVersion)

  private fun toTokens(record: IpAddressRecord): LongArray =
      if (record.ipVersion == IpVersion.IPv4) longArrayOf((record.ipNumberLow shl 32).toLong())
      else longArrayOf(record.ipNumberHigh.toLong(), record.ipNumberLow.toLong())
}
