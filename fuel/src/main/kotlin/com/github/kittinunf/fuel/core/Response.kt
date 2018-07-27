package com.github.kittinunf.fuel.core

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.URLConnection
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class Response(
        val url: URL,
        val statusCode: Int = -1,
        val responseMessage: String = "",
        val headers: Map<String, List<String>> = emptyMap(),
        val contentLength: Long = 0L,
        val dataStream: InputStream = ByteArrayInputStream(ByteArray(0))

) {
    @Deprecated(replaceWith = ReplaceWith("contentLength"), message = "http naming is deprecated, use 'contentLength' instead")
    val httpContentLength
        get() = contentLength

    @Deprecated(replaceWith = ReplaceWith("responseMessage"), message = "http naming is deprecated, use 'responseMessage' instead")
    val httpResponseMessage
        get() = responseMessage

    @Deprecated(replaceWith = ReplaceWith("statusCode"), message = "http naming is deprecated, use 'statusCode' instead")
    val httpStatusCode
        get() = statusCode

    @Deprecated(replaceWith = ReplaceWith("headers"), message = "http naming is deprecated, use 'headers' instead")
    val httpResponseHeaders
        get() = headers


    var data: ByteArray by MutableLazy {
        try {
            dataStream.readBytes()
        } catch (ex: IOException) {  // If dataStream closed by deserializer
            print("empty arry")
            ByteArray(0)
        }
    }

    override fun toString(): String {
        val contentType = guessContentType(headers)
        val dataString = processBody(contentType, data)

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

    internal fun guessContentType(headers: Map<String, List<String>>): String {
        val contentTypeFromHeaders = headers["Content-Type"]?.first()
        if (contentTypeFromHeaders is String && !contentTypeFromHeaders.isNullOrEmpty()) {
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

class MutableLazy<T>(val init: () -> T) : ReadWriteProperty<Any?, T> {

    private var value: Optional<T> = Optional.None()

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (value is Optional.None) {
            value = Optional.Some(init())
        }
        return value.get()
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = Optional.Some(value)
    }
}

sealed class Optional<out T> {

    abstract fun get(): T

    class Some<out T>(val value: T) : Optional<T>() {
        override fun get() = value
    }
    class None<out T> : Optional<T>() {
        override fun get(): T {
            throw NoSuchElementException("Can't get object from Optional.None")
        }
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
