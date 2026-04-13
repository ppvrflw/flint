package me.ppvrflw.record

import me.ppvrflw.DomainMatchable
import me.ppvrflw.matcher.DomainNameMatcher

/**
 * Represents an email address, split into a local part and a [DomainNameRecord].
 *
 * Matching is performed against the domain part only. Use the companion object's [from] method to
 * parse email strings with validation, or [fromTrusted] to skip validation for performance-critical
 * paths.
 *
 * @property domainNameRecord The parsed domain part of the email address.
 * @property localPart The local part of the email address (before the @ symbol).
 */
@ConsistentCopyVisibility
data class EmailRecord
private constructor(
    val domainNameRecord: DomainNameRecord,
    val localPart: String,
) : DomainMatchable {
  override fun <V> match(matcher: DomainNameMatcher<V>): List<V> {
    return matcher.match(domainNameRecord)
  }

  companion object : RecordFactory<EmailRecord> {

    /**
     * Parses an email string into an [EmailRecord] with full validation.
     *
     * @param raw The email string to parse (e.g., "user@example.com").
     * @return A new [EmailRecord] instance.
     * @throws IllegalArgumentException if the email is empty, missing an @ symbol, or has an empty
     *   local part or hostname.
     */
    override fun from(raw: String): EmailRecord {
      require(raw.isNotEmpty()) { "email cannot be empty" }

      val parts = raw.split("@")
      require(parts.size == 2) { "email must have exactly one @ symbol" }

      val localPart = parts[0]
      require(localPart.isNotEmpty()) { "local part cannot be empty" }

      val hostname = parts[1]
      require(hostname.isNotEmpty()) { "hostname cannot be empty" }

      return EmailRecord(
          domainNameRecord = DomainNameRecord.from(hostname),
          localPart = localPart,
      )
    }

    /**
     * Parses an email string into an [EmailRecord], skipping validation.
     *
     * Use this when the input is already known to be valid (e.g., from a trusted data source or
     * after prior validation) and performance is critical.
     *
     * @param raw A pre-validated email string (e.g., "user@example.com").
     * @return A new [EmailRecord] instance.
     */
    override fun fromTrusted(raw: String): EmailRecord {
      val atIndex = raw.indexOf('@')
      return EmailRecord(
          domainNameRecord = DomainNameRecord.fromTrusted(raw.substring(atIndex + 1)),
          localPart = raw.substring(0, atIndex),
      )
    }
  }
}
