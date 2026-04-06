package me.ppvrflw

import me.ppvrflw.matcher.FileHashMatcher

interface FileHashMatchable {

  /**
   * Matches a record against a given [FileHashMatcher].
   *
   * @param matcher the [FileHashMatcher] to associate the value with
   */
  fun <V> match(matcher: FileHashMatcher<V>): List<V>
}
