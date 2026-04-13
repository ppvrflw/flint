package me.ppvrflw.record

/**
 * Factory contract for record types that can be created from a raw string.
 *
 * Implementations provide two creation methods: [from] with full validation and [fromTrusted]
 * without validation for performance-critical paths.
 *
 * @param T The record type produced by this factory.
 */
interface RecordFactory<T> {

  /**
   * Creates a record from a raw string with full validation.
   *
   * @param raw The raw string to parse and validate.
   * @return A new record instance.
   * @throws IllegalArgumentException if the input is invalid.
   */
  fun from(raw: String): T

  /**
   * Creates a record from a trusted raw string, skipping validation.
   *
   * Use this when the input is already known to be valid (e.g., from a trusted data source or after
   * prior validation) and performance is critical.
   *
   * @param raw The pre-validated raw string to parse.
   * @return A new record instance.
   */
  fun fromTrusted(raw: String): T
}
