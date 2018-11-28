package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.core.requests.DefaultBody
import com.github.kittinunf.result.Result
import java.net.URL

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
        return buildString {
            appendln("<-- $statusCode $url")
            appendln("Response : $responseMessage")
            appendln("Length : $contentLength")
            appendln("Body : ${body.asString(headers[Headers.CONTENT_TYPE].lastOrNull())}")
            appendln("Headers : (${headers.size})")

            val appendHeaderWithValue = { key: String, value: String -> appendln("$key : $value") }
            headers.transformIterate(appendHeaderWithValue)
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
