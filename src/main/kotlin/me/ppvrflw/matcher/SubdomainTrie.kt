package me.ppvrflw.matcher

/**
 * A trie (prefix tree) for matching subdomain hierarchies.
 *
 * Subdomain parts are stored in reverse order, so that lookups traverse from the most general label
 * to the most specific. Each node along the path may hold one or more values, allowing partial
 * (suffix) matches to be collected in a single traversal.
 *
 * @param V the type of value associated with subdomain entries
 */
internal class SubdomainTrie<V> {

  private val root = TrieNode<V>()

  /**
   * Inserts a value into the trie for the given subdomain parts.
   *
   * @param subDomainParts the subdomain labels in natural order (e.g. `["comments", "blog"]`)
   * @param value the value to associate with this domain path
   */
  fun insert(subDomainParts: List<String>, value: V) {
    val node =
        if (subDomainParts.isEmpty()) root
        else
            subDomainParts.asReversed().fold(root) { current, token ->
              current.children.getOrPut(token) { TrieNode() }
            }

    if (value !in node.values) {
      node.values.add(value)
    }
  }

  /**
   * Removes a value from the node matching the given [subDomainParts].
   *
   * @param subDomainParts the subdomain labels in natural order, or an empty list for the root
   * @param value the value to remove
   */
  fun remove(subDomainParts: List<String>, value: V) {
    val node =
        if (subDomainParts.isEmpty()) root
        else
            subDomainParts.asReversed().fold<String, TrieNode<V>?>(root) { current, token ->
              current?.children?.get(token)
            }

    node?.values?.remove(value)
  }

  /**
   * Returns all values whose subdomain path is a suffix of the given [subDomainParts].
   *
   * @param subDomainParts the domain labels to match in natural order, or `null`
   * @return all values associated with matching suffix paths
   */
  fun match(subDomainParts: List<String>?): List<V> {
    if (subDomainParts.isNullOrEmpty()) return root.values

    val matches = root.values.toMutableList()
    var current: TrieNode<V>? = root

    subDomainParts.asReversed().forEach { token ->
      current = current?.children?.get(token)
      current?.let { if (it.values.isNotEmpty()) matches.addAll(it.values) }
    }

    return matches
  }

  /** A single node in the subdomain trie, holding child branches and associated values. */
  private class TrieNode<V> {
    val children: MutableMap<String, TrieNode<V>> = mutableMapOf()
    val values: MutableList<V> = mutableListOf()
  }
}
