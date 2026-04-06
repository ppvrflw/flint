package me.ppvrflw.record

/**
 * Represents a file hash as a lowercase hexadecimal string.
 *
 * The hash value must be non-blank and consist only of lowercase characters. Use the companion
 * object's `from` method to create instances with automatic lowercase conversion.
 *
 * @property hash A non-blank, lowercase hexadecimal string.
 * @throws IllegalArgumentException if the hash is blank or contains uppercase characters.
 */
data class FileHashRecord(val hash: String) {
  init {
    require(hash.isNotBlank()) { "hash can't be empty" }
    require(hash == hash.lowercase()) { "hash must be lowercase" }
  }

  companion object {

    /**
     * Creates a [FileHashRecord] from a hash string with automatic lowercase conversion.
     *
     * @param hash A hexadecimal hash string that will be converted to lowercase.
     * @return A new [FileHashRecord] instance with the normalized hash value.
     */
    fun from(hash: String): FileHashRecord = FileHashRecord(hash.lowercase())
  }
}
