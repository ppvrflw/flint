package me.ppvrflw

/**
 * Core interface for matching keys against stored entries and retrieving associated values.
 *
 * Implementations are not thread-safe. Callers must synchronize externally if accessed from
 * multiple threads.
 *
 * @param K the key type used for indexing and matching
 * @param V the type of value associated with each key
 */
interface Matcher<K, V> {

  /**
   * Inserts a value associated with the given key into the matcher.
   *
   * @param key the key to associate the value with
   * @param value the value to store
   */
  fun insert(key: K, value: V)

  /**
   * Inserts all key-value pairs from the given [entries].
   *
   * @param entries the key-value pairs to insert
   * @see insert
   */
  fun insertAll(entries: Iterable<Pair<K, V>>) {
    entries.forEach { (key, value) -> insert(key, value) }
  }

  /**
   * Removes a value associated with the given key from the matcher.
   *
   * @param key the key the value is associated with
   * @param value the value to remove
   */
  fun remove(key: K, value: V)

  /**
   * Matches the given key against all stored entries and returns all associated values.
   *
   * @param key the key to match against
   * @return a list of all values whose keys match the given key
   */
  fun match(key: K): List<V>
}
