package me.ppvrflw.matcher

import me.ppvrflw.AbstractMatcher
import me.ppvrflw.Matcher
import me.ppvrflw.record.FileHashRecord

/**
 * A [Matcher] that indexes and matches values by file hash.
 *
 * Uses a simple hash-to-values map for exact-match lookups. Duplicate values for the same hash are
 * ignored on insert, and empty entries are cleaned up on remove.
 *
 * @param V the type of value associated with file hash entries
 */
class FileHashMatcher<V> : AbstractMatcher<FileHashRecord, V>(FileHashRecord::from) {
  private val fileHashMap: MutableMap<String, MutableList<V>> = mutableMapOf()

  override fun insert(key: FileHashRecord, value: V) {
    val values = fileHashMap.getOrPut(key.hash) { mutableListOf() }
    if (value !in values) {
      values.add(value)
    }
  }

  override fun remove(key: FileHashRecord, value: V) {
    val values = fileHashMap[key.hash] ?: return
    values.remove(value)
    if (values.isEmpty()) {
      fileHashMap.remove(key.hash)
    }
  }

  override fun match(key: FileHashRecord): List<V> = fileHashMap[key.hash]?.toList() ?: emptyList()
}
