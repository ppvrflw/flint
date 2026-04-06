package me.ppvrflw.record

import me.ppvrflw.DomainMatchable
import me.ppvrflw.matcher.DomainNameMatcher

private const val MIN_DOMAIN_PARTS = 2

/**
 * Represents a parsed domain name with its hierarchical components.
 *
 * Stores the top-level domain (TLD), second-level domain, and optional subdomain hierarchy.
 * Subdomains are stored in order from leftmost to rightmost (e.g., "api.sub.example.com" stores
 * ["api", "sub"]). Use the companion object's `from` method to parse hostname strings.
 *
 * @property tld The top-level domain (e.g., "com", "org").
 * @property domain The second-level domain name (e.g., "example").
 * @property subdomainParts Optional list of subdomain components in left-to-right order, or null if
 *   no subdomains exist.
 */
data class DomainNameRecord(
    val tld: String,
    val domain: String,
    val subdomainParts: List<String>?,
) : DomainMatchable {

  override fun <V> match(matcher: DomainNameMatcher<V>): List<V> {
    return matcher.match(this)
  }

  companion object {

    /**
     * Parses a hostname string into a [DomainNameRecord].
     *
     * Extracts the TLD, domain, and subdomain hierarchy from the given hostname. The hostname must
     * contain at least a domain and TLD (e.g., "example.com"). Subdomains are stored in reverse
     * order (e.g., "api.sub.example.com" stores ["api", "sub"]).
     *
     * @param hostname The hostname string to parse (e.g., "www.example.com").
     * @return A [DomainNameRecord] containing the parsed domain components.
     * @throws IllegalArgumentException if the hostname is empty or has fewer than two parts.
     */
    fun from(hostname: String): DomainNameRecord {
      require(hostname.isNotEmpty()) { "hostname cannot be empty" }

      val parts = hostname.split(".")
      require(parts.size >= MIN_DOMAIN_PARTS) { "hostname must have at least domain and tld" }

      val tld = parts.last()
      val domain = parts[parts.lastIndex - 1]

      return when (parts.size) {
        MIN_DOMAIN_PARTS -> DomainNameRecord(tld, domain, null)
        else -> DomainNameRecord(tld, domain, parts.dropLast(2))
      }
    }
  }
}
