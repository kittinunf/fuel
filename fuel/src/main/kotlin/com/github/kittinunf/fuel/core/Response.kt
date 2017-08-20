package com.github.kittinunf.fuel.core

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL

class Response(
        val url: URL,
        val httpStatusCode: Int = -1,
        val httpResponseMessage: String = "",
        val httpResponseHeaders: Map<String, List<String>> = emptyMap<String, List<String>>(),
        val httpContentLength: Long = 0L,
        val dataStream: InputStream = ByteArrayInputStream(ByteArray(0))
) {
    val data: ByteArray by lazy {
        try {
            dataStream.use { dataStream.readBytes() }
        } catch (ex: IOException) {  // If dataStream closed by deserializer
            ByteArray(0)
        }
    }

    override fun toString(): String = buildString {
        appendln("<-- $httpStatusCode ($url)")
        appendln("Response : $httpResponseMessage")
        appendln("Length : $httpContentLength")
        appendln("Body : ${if (data.isNotEmpty()) String(data) else "(empty)"}")
        appendln("Headers : (${httpResponseHeaders.size})")
        for ((key, value) in httpResponseHeaders) {
            appendln("$key : $value")
        }
    }

    companion object {
        fun error(): Response = Response(URL("http://."))
    }
}
