package me.ppvrflw.matcher

import me.ppvrflw.record.DomainNameRecord
import me.ppvrflw.Matcher

/**
 * A [Matcher] that indexes and matches values by domain name, supporting suffix-based subdomain
 * matching.
 *
 * Domains are partitioned by TLD and second-level domain, with subdomain hierarchies stored in a
 * [SubdomainTrie]. Matching a domain returns all values registered at that exact level and any
 * parent levels (e.g. querying `api.example.com` returns values from both `api.example.com` and
 * `example.com`).
 *
 * @param V the type of value associated with domain entries
 */
class DomainNameMatcher<V> : Matcher<DomainNameRecord, V> {

  private val tldDomainTrieMap: MutableMap<String, MutableMap<String, SubdomainTrie<V>>> =
      mutableMapOf()

  override fun insert(key: DomainNameRecord, value: V) {
    val trie =
        tldDomainTrieMap
            .getOrPut(key.tld) { mutableMapOf() }
            .getOrPut(key.domain) { SubdomainTrie() }

    trie.insert(key.subdomainParts.orEmpty(), value)
  }

  override fun remove(key: DomainNameRecord, value: V) {
    val trie = tldDomainTrieMap[key.tld]?.get(key.domain) ?: return

    trie.remove(key.subdomainParts.orEmpty(), value)
  }

  override fun match(key: DomainNameRecord): List<V> {
    val trie = tldDomainTrieMap[key.tld]?.get(key.domain) ?: return emptyList()

    return trie.match(key.subdomainParts)
  }
}
