package me.ppvrflw

import me.ppvrflw.matcher.IpMatcher

interface IpMatchable {

  /**
   * Matches a record against a given [IpMatcher].
   *
   * @param matcher the [IpMatcher] to associate the value with
   */
  fun <V> match(matcher: IpMatcher<V>): List<V>
}
