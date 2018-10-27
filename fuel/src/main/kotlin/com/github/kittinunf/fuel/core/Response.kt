package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.util.readWriteLazy
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.URLConnection

data class Response(
    val url: URL,
    val statusCode: Int = -1,
    val responseMessage: String = "",
    val headers: Headers = Headers(),
    val contentLength: Long = 0L,
    val dataStream: InputStream = ByteArrayInputStream(ByteArray(0))
) {
    var data: ByteArray by readWriteLazy {
        try {
            dataStream.readBytes()
        } catch (ex: IOException) { // If dataStream closed by deserializer
            ByteArray(0)
        }
    }

    /**
     * Get the current values of the header, after normalisation of the header
     * @param header [String] the header name
     * @return the current values (or empty if none)
     */
    operator fun get(header: String): HeaderValues {
        return headers[header]
    }

    fun header(header: String) = get(header)

    override fun toString(): String {
        val contentType = guessContentType()
        val dataString = processBody(contentType, data)

        return buildString {
            appendln("<-- $statusCode ($url)")
            appendln("Response : $responseMessage")
            appendln("Length : $contentLength")
            appendln("Body : ($dataString)")
            appendln("Headers : (${headers.size})")

            val appendHeaderWithValue = { key: String, value: String -> appendln("$key : $value") }
            headers.transformIterate(appendHeaderWithValue)
        }
    }

    internal fun guessContentType(headers: Headers = this.headers): String {
        val contentTypeFromHeaders = headers[Headers.CONTENT_TYPE].lastOrNull()
        if (!contentTypeFromHeaders.isNullOrEmpty()) {
            return contentTypeFromHeaders
        }

        val contentTypeFromStream = URLConnection.guessContentTypeFromStream(ByteArrayInputStream(data))
        return if (contentTypeFromStream.isNullOrEmpty()) "(unknown)" else contentTypeFromStream
    }

    internal fun processBody(contentType: String, bodyData: ByteArray): String {
        return if (contentType.isNotEmpty() &&
                (contentType.contains("image/") ||
                        contentType.contains("application/octet-stream"))) {
            "$contentLength bytes of ${guessContentType(headers)}"
        } else if (bodyData.isNotEmpty()) {
            String(bodyData)
        } else {
            "(empty)"
        }
    }

    companion object {
        fun error(): Response = Response(URL("http://."))
    }
}

val Response.isStatusInformational
    get() = (statusCode / 100) == 1

val Response.isSuccessful
    get() = (statusCode / 100) == 2

val Response.isStatusRedirection
    get() = (statusCode / 100) == 3

val Response.isClientError
    get() = (statusCode / 100) == 4

val Response.isServerError
    get() = (statusCode / 100) == 5
