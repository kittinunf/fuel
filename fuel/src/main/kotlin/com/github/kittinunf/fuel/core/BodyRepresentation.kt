package com.github.kittinunf.fuel.core

import java.nio.charset.Charset
import java.nio.charset.IllegalCharsetNameException

private val TEXT_CONTENT_TYPE = Regex(
        "^(?:text/.*|application/(?:csv|javascript|json|typescript|xml|x-yaml|x-www-form-urlencoded|vnd\\.coffeescript)|.*\\+(?:xml|json))"
)

/**
 * Represents a number of bytes either as content, as (unknown), as (empty) or as (x bytes of type).
 *
 * @param contentType [String?] the content type or null if unknown
 * @return [String] the representation
 */
fun Body.representationOfBytes(contentType: String?): String {
    val actualContentType = if (contentType.isNullOrEmpty()) "(unknown)" else contentType

    if (TEXT_CONTENT_TYPE.containsMatchIn(actualContentType)) {
        val charsetRegex = Regex("^CHARSET=.*")
        val parameters = actualContentType
                .toUpperCase()
                .split(';')
                .map { it.trim() }

        val charset: Charset = try {
            Charset.forName(parameters.find { charsetRegex.matches(it) }?.substringAfter("CHARSET=") ?: "")
        } catch (e: IllegalCharsetNameException) {
            Charsets.US_ASCII
        }

        return String(toByteArray(), charset)
    }

    val lengthLabel = (length ?: -1L).let {
        when {
            it == 0L -> return "(empty)"
            it < 0L -> "unknown number of bytes"
            else -> "$it bytes"
        }
    }

    return "($lengthLabel of $actualContentType)"
}
