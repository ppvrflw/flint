package me.ppvrflw.matcher

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import me.ppvrflw.Matcher

/**
 * A thread-safe wrapper around any [me.ppvrflw.Matcher] implementation.
 *
 * Uses a [java.util.concurrent.locks.ReentrantReadWriteLock] to allow concurrent reads while
 * serializing writes.
 *
 * @param K the key type
 * @param V the value type
 * @param delegate the underlying matcher to synchronize access to
 */
class SynchronizedMatcher<K, V>(
    private val delegate: Matcher<K, V>,
) : Matcher<K, V> {

  private val lock = ReentrantReadWriteLock()

  override fun insert(key: K, value: V) {
    lock.write { delegate.insert(key, value) }
  }

  override fun remove(key: K, value: V) {
    lock.write { delegate.remove(key, value) }
  }

  /**
   * Inserts all key-value pairs under a single write lock.
   *
   * @param entries the key-value pairs to insert
   */
  fun insertAll(entries: List<Pair<K, V>>) {
    lock.write { entries.forEach { (key, value) -> delegate.insert(key, value) } }
  }

  /**
   * Removes all key-value pairs under a single write lock.
   *
   * @param entries the key-value pairs to remove
   */
  fun removeAll(entries: List<Pair<K, V>>) {
    lock.write { entries.forEach { (key, value) -> delegate.remove(key, value) } }
  }

  override fun match(key: K): List<V> {
    lock.read {
      return delegate.match(key)
    }
  }
}
