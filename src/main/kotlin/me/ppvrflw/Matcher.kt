package me.ppvrflw

/**
 * Implementations are not thread-safe. Callers must synchronize externally if accessed from
 * multiple threads.
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
