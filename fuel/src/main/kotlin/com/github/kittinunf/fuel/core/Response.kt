package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.core.requests.DefaultBody
import com.github.kittinunf.result.Result
import java.io.ByteArrayInputStream
import java.net.URL
import java.net.URLConnection

/**
 * Response object that holds [Request] and [Response] metadata, as well as the result T
 *
 * @see ResponseResultOf
 * @see ResponseHandler
 */
typealias ResponseOf<T> = Triple<Request, Response, T>

/**
 * Response object that holds [Request] and [Response] metadata, as well as a [Result] wrapping T or [FuelError]
 *
 * @see ResponseOf
 * @see ResponseResultHandler
 */
typealias ResponseResultOf<T> = Triple<Request, Response, Result<T, FuelError>>

data class Response(
    val url: URL,
    val statusCode: Int = -1,
    val responseMessage: String = "",
    val headers: Headers = Headers(),
    val contentLength: Long = 0L,
    internal var body: Body = DefaultBody()
) {
    fun body(): Body = body
    val data get() = body.toByteArray()

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

        val bodyString = when {
            body.isEmpty() -> "empty"
            body.isConsumed() -> "consumed"
            else -> processBody(contentType, body.toByteArray())
        }

        return buildString {
            appendln("<-- $statusCode ($url)")
            appendln("Response : $responseMessage")
            appendln("Length : $contentLength")
            appendln("Body : ($bodyString)")
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
        fun error(url: URL = URL("http://.")): Response = Response(url)
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
