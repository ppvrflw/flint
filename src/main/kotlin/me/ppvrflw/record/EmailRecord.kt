package me.ppvrflw.record

import me.ppvrflw.DomainMatchable
import me.ppvrflw.matcher.DomainNameMatcher

data class EmailRecord(
    val domainNameRecord: DomainNameRecord,
    val localPart: String,
) : DomainMatchable {
  override fun <V> match(matcher: DomainNameMatcher<V>): List<V> {
    return matcher.match(domainNameRecord)
  }

  companion object {

    fun from(email: String): EmailRecord {
      require(email.isNotEmpty()) { "email cannot be empty" }

      val parts = email.split("@")
      require(parts.size == 2) { "email must have exactly one @ symbol" }

      val hostname = parts[1]
      require(hostname.isNotEmpty()) { "hostname cannot be empty" }

      return EmailRecord(
          domainNameRecord = DomainNameRecord.from(hostname),
          localPart = parts[0],
      )
    }
  }
}
