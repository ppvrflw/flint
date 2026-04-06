package me.ppvrflw

import me.ppvrflw.matcher.DomainNameMatcher

interface DomainMatchable {

  /**
   * Matches a record against a given [DomainNameMatcher].
   *
   * @param matcher the [DomainNameMatcher] to associate the value with
   */
  fun <V> match(matcher: DomainNameMatcher<V>): List<V>
}
