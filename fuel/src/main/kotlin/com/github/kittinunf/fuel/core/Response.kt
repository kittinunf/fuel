package com.github.kittinunf.fuel.core

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.URLConnection

class Response(
        val url: URL,
        val statusCode: Int = -1,
        val responseMessage: String = "",
        val headers: Map<String, List<String>> = emptyMap(),
        val contentLength: Long = 0L,
        val dataStream: InputStream = ByteArrayInputStream(ByteArray(0))
) {
    @Deprecated(replaceWith = ReplaceWith("contentLength"), message = "http naming is deprecated, use 'contentLength' instead")
    val httpContentLength get() = contentLength

    @Deprecated(replaceWith = ReplaceWith("responseMessage"), message = "http naming is deprecated, use 'responseMessage' instead")
    val httpResponseMessage get() = responseMessage

    @Deprecated(replaceWith = ReplaceWith("statusCode"), message = "http naming is deprecated, use 'statusCode' instead")
    val httpStatusCode get() = statusCode

    @Deprecated(replaceWith = ReplaceWith("headers"), message = "http naming is deprecated, use 'headers' instead")
    val httpResponseHeaders get() = headers

    val data: ByteArray by lazy {
        try {
            dataStream.readBytes()
        } catch (ex: IOException) {  // If dataStream closed by deserializer
            ByteArray(0)
        }
    }

    override  fun toString(): String {
        var dataString = "(empty)"
        val contentType = guessContentType()

        if (contentType.isNotEmpty() &&
                (contentType.contains("image/") ||
                        contentType.contains("application/octet-stream")
                        )) {
            dataString = "$contentLength bytes of ${guessContentType()}"
        } else if (data.isNotEmpty()) {
            dataString = String(data)
        }

        return buildString {
            appendln("<-- $statusCode ($url)")
            appendln("Response : $responseMessage")
            appendln("Length : $contentLength")
            appendln("Body : ($dataString)")
            appendln("Headers : (${headers.size})")
            for ((key, value) in headers) {
                appendln("$key : $value")
            }
        }
    }

    fun guessContentType(): String {
        val contentTypeFromHeaders = headers["Content-Type"]?.first()
        if (contentTypeFromHeaders is String) {
            return contentTypeFromHeaders
        }

        val contentTypeFromStream = URLConnection.guessContentTypeFromStream(ByteArrayInputStream(data))
        return if(contentTypeFromStream.isNullOrEmpty()) "(unknown)" else contentTypeFromStream
    }

    companion object {
        fun error(): Response = Response(URL("http://."))
    }
}
