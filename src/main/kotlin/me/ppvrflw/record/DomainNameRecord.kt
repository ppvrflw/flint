package me.ppvrflw.record

import me.ppvrflw.DomainMatchable
import me.ppvrflw.matcher.DomainNameMatcher

/**
 * Represents a parsed domain name with its hierarchical components.
 *
 * Stores the top-level domain (TLD), second-level domain, and optional subdomain hierarchy.
 * Subdomains are stored in order from leftmost to rightmost (e.g., "api.sub.example.com" stores
 * ["api", "sub"]). Use the companion object's [from] method to parse hostname strings with
 * validation, or [fromTrusted] to skip validation for performance-critical paths.
 *
 * @property tld The top-level domain (e.g., "com", "org").
 * @property domain The second-level domain name (e.g., "example").
 * @property subdomainParts Optional list of subdomain components in left-to-right order, or null if
 *   no subdomains exist.
 */
@ConsistentCopyVisibility
data class DomainNameRecord
private constructor(
    val tld: String,
    val domain: String,
    val subdomainParts: List<String>?,
) : DomainMatchable {

  override fun <V> match(matcher: DomainNameMatcher<V>): List<V> {
    return matcher.match(this)
  }

  companion object : RecordFactory<DomainNameRecord> {
    private const val MIN_DOMAIN_PARTS = 2

    /**
     * Parses a hostname string into a [DomainNameRecord].
     *
     * Extracts the TLD, domain, and subdomain hierarchy from the given hostname. The hostname must
     * contain at least a domain and TLD (e.g., "example.com"). Subdomains are stored in
     * left-to-right order (e.g., "api.sub.example.com" stores ["api", "sub"]).
     *
     * @param raw The hostname string to parse (e.g., "www.example.com").
     * @return A [DomainNameRecord] containing the parsed domain components.
     * @throws IllegalArgumentException if the hostname is empty or has fewer than two parts.
     */
    override fun from(raw: String): DomainNameRecord {
      require(raw.isNotEmpty()) { "hostname cannot be empty" }
      require(raw == raw.trim()) { "hostname cannot contain leading or trailing whitespace" }

      val parts = raw.lowercase().split(".")
      require(parts.none { it.isEmpty() }) { "hostname parts cannot be empty" }
      require(parts.size >= MIN_DOMAIN_PARTS) { "hostname must have at least domain and tld" }

      return parse(parts)
    }

    /**
     * Parses a hostname string into a [DomainNameRecord], skipping validation.
     *
     * Use this when the input is already known to be valid (e.g., from a trusted data source or
     * after prior validation) and performance is critical. Assumes the hostname is already
     * lowercase and has no leading/trailing dots.
     *
     * @param raw A pre-validated hostname string (e.g., "www.example.com").
     * @return A new [DomainNameRecord] instance.
     */
    override fun fromTrusted(raw: String): DomainNameRecord {
      return parse(raw.split("."))
    }

    private fun parse(parts: List<String>): DomainNameRecord {
      val tld = parts.last()
      val domain = parts[parts.lastIndex - 1]

      return when (parts.size) {
        MIN_DOMAIN_PARTS -> DomainNameRecord(tld, domain, null)
        else -> DomainNameRecord(tld, domain, parts.dropLast(2))
      }
    }
  }
}
