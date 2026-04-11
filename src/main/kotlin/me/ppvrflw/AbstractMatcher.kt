package me.ppvrflw

/**
 * Base class for [Matcher] implementations that provides bulk loading from raw string keys.
 *
 * Subclasses supply a [parser] function at construction time that converts raw strings into the
 * matcher's key type. This keeps parsing responsibility on the record layer while giving every
 * matcher a consistent [insertAllRaw] API for free.
 *
 * @param K the key type used for indexing and matching
 * @param V the type of value associated with each key
 * @param parser a function that converts a raw string into a key of type [K]
 */
abstract class AbstractMatcher<K, V>(private val parser: (String) -> K) : Matcher<K, V> {

  /**
   * Parses and inserts all key-value pairs from the given [entries].
   *
   * Each string key is converted to [K] using the [parser] supplied at construction time.
   *
   * @param entries the raw string key-value pairs to insert
   * @see insert
   */
  fun insertAllRaw(entries: Iterable<Pair<String, V>>) {
    entries.forEach { (raw, value) -> insert(parser(raw), value) }
  }

  /**
   * Parses and inserts all key-value pairs from the given [entries] sequence.
   *
   * Processes entries lazily, making this suitable for large data sources such as files or streams
   * where holding all entries in memory is not desirable.
   *
   * @param entries a lazy sequence of raw string key-value pairs to insert
   * @see insert
   */
  fun insertAllRaw(entries: Sequence<Pair<String, V>>) {
    entries.forEach { (raw, value) -> insert(parser(raw), value) }
  }
}
