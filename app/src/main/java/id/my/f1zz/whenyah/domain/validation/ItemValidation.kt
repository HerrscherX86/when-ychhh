package id.my.f1zz.whenyah.domain.validation

import java.net.URI

object ItemValidation {
    /**
     * Parses a non-negative integer rupiah amount. Fractional values, signs,
     * separators, and values that overflow [Long] are rejected.
     */
    fun parseIdrAmount(input: String): Long? {
        val trimmed = input.trim()
        if (trimmed.isEmpty() || trimmed.any { !it.isDigit() }) return null
        return trimmed.toLongOrNull()
    }

    fun isValidName(name: String): Boolean = name.isNotBlank()

    /**
     * An empty purchase URL is allowed. Otherwise only absolute HTTP(S) URLs
     * with a host are accepted.
     */
    fun isValidPurchaseUrl(input: String): Boolean {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return true

        val uri = runCatching { URI(trimmed) }.getOrNull() ?: return false
        return uri.scheme?.lowercase() in ALLOWED_SCHEMES && !uri.host.isNullOrBlank()
    }

    private val ALLOWED_SCHEMES = setOf("http", "https")
}
