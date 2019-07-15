package com.github.kittinunf.fuel.core

import java.nio.charset.Charset

private val TEXT_CONTENT_TYPE = Regex(
    "^(?:text/.*|application/(?:csv|javascript|json|typescript|xml|x-yaml|x-www-form-urlencoded|vnd\\.coffeescript)|.*\\+(?:xml|json))(; charset=.+)*$"
)

/**
 * Represents a number of bytes either as content, as (unknown), as (empty) or as (x bytes of type).
 *
 * @param contentType [String?] the content type or null if unknown
 * @return [String] the representation
 */
fun Body.representationOfBytes(contentType: String?): String {
    val actualContentType = if (contentType.isNullOrEmpty()) "(unknown)" else contentType

    if (TEXT_CONTENT_TYPE.matches(actualContentType)) {
        var charset = Charsets.UTF_8
        val charsetGroup = TEXT_CONTENT_TYPE.find(actualContentType)!!.groupValues[1]
        if (charsetGroup.isNotEmpty()) {
            val charsetName = charsetGroup.substringAfter('=').toUpperCase()
            charset = Charset.forName(charsetName)
        }
        return String(toByteArray(), charset)
    }

    val lengthLabel = (length ?: -1L).let {
        when (true) {
            it == 0L -> return "(empty)"
            it < 0L -> "unknown number of bytes"
            else -> "$it bytes"
        }
    }

    return "($lengthLabel of $actualContentType)"
}
