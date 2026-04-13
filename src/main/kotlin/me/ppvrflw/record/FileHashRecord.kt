package me.ppvrflw.record

import me.ppvrflw.FileHashMatchable
import me.ppvrflw.matcher.FileHashMatcher

/**
 * Represents a file hash as a lowercase hexadecimal string.
 *
 * Use the companion object's [from] method to create instances with validation and automatic
 * lowercase conversion, or [fromTrusted] to skip validation for performance-critical paths.
 *
 * @property hash A non-blank, lowercase hexadecimal string.
 */
@ConsistentCopyVisibility
data class FileHashRecord private constructor(val hash: String) : FileHashMatchable {
  override fun <V> match(matcher: FileHashMatcher<V>): List<V> {
    return matcher.match(this)
  }

  companion object : RecordFactory<FileHashRecord> {

    /**
     * Creates a [FileHashRecord] from a hash string with automatic lowercase conversion and
     * validation.
     *
     * @param raw A hexadecimal hash string that will be converted to lowercase.
     * @return A new [FileHashRecord] instance with the normalized hash value.
     * @throws IllegalArgumentException if the hash is blank or not hexadecimal.
     */
    override fun from(raw: String): FileHashRecord {
      require(raw.isNotBlank()) { "hash can't be empty" }
      val lowered = raw.lowercase()
      require(lowered.all { it in '0'..'9' || it in 'a'..'f' }) { "hash must be hexadecimal" }
      return FileHashRecord(lowered)
    }

    /**
     * Creates a [FileHashRecord] from a trusted lowercase hexadecimal string, skipping validation.
     *
     * Use this when the input is already known to be valid (e.g., from a trusted data source or
     * after prior validation) and performance is critical.
     *
     * @param raw A pre-validated, lowercase hexadecimal hash string.
     * @return A new [FileHashRecord] instance.
     */
    override fun fromTrusted(raw: String): FileHashRecord = FileHashRecord(raw)
  }
}
