package com.github.kittinunf.fuel.toolbox

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.DefaultBody
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.HeaderName
import com.github.kittinunf.fuel.core.HeaderValues
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.Proxy
import java.net.URLConnection
import java.util.zip.GZIPInputStream
import java.util.zip.InflaterInputStream
import javax.net.ssl.HttpsURLConnection
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class HttpClient(
    private val proxy: Proxy? = null,
    var useHttpCache: Boolean = true,
    var decodeContent: Boolean = true
) : Client {
    override fun executeRequest(request: Request): Response {
        try {
            return doRequest(request)
        } catch (exception: Exception) {
            throw FuelError(exception, ByteArray(0), Response(request.url))
        } finally {
            // As per Android documentation, a connection that is not explicitly disconnected
            // will be pooled and reused!  So, don't close it as we need inputStream later!
            // connection.disconnect()
        }
    }

    override suspend fun awaitRequest(request: Request): Response = suspendCoroutine { continuation ->
        try {
            continuation.resume(doRequest(request))
        } catch (exception: Exception) {
            continuation.resumeWithException(exception as? FuelError
                ?: FuelError(exception, ByteArray(0), Response(request.url)))
        }
    }

    @Throws
    private fun doRequest(request: Request): Response {
        val connection = establishConnection(request) as HttpURLConnection
        connection.apply {
            connectTimeout = Fuel.testConfiguration.coerceTimeout(request.timeoutInMillisecond)
            readTimeout = Fuel.testConfiguration.coerceTimeoutRead(request.timeoutReadInMillisecond)
            requestMethod = HttpClient.coerceMethod(request.method).value
            doInput = true
            useCaches = request.useHttpCache ?: useHttpCache
            instanceFollowRedirects = false

            request.headers.transformIterate(
                { key, values -> setRequestProperty(key, values) },
                { key, value -> addRequestProperty(key, value) }
            )

            // By default, the Android implementation of HttpURLConnection requests that servers use gzip compression
            //   and it automatically decompresses the data for callers of URLConnection.getInputStream().
            //   The Content-Encoding and Content-Length response headers are cleared in this case. Gzip compression can
            //   be disabled by setting the acceptable encodings in the request header:
            //
            //      .header(Headers.ACCEPT_ENCODING, "identity")
            //
            // However, on the JVM, this behaviour might be different. Content-Encoding SHOULD NOT be used, in HTTP/1x
            //  to act as Transfer Encoding. In HTTP/2, Transfer-Encoding is not part of the Connection field and should
            //  not be injected here. HttpURLConnection is only HTTP/1x, whereas Java 9 introduces a new HttpClient for
            //  HTTP/2.
            //
            // This adds the TE header for HTTP/1 connections, and automatically decodes it using decodeTransfer.

            // The TE (Accept Transfer Encoding) can only be one of these, should match decodeTransfer.
            setRequestProperty(
                Headers.ACCEPT_TRANSFER_ENCODING,
                Headers.collapse(HeaderName(Headers.ACCEPT_TRANSFER_ENCODING), SUPPORTED_DECODING)
            )

            // The underlying HttpURLConnection does not support PATCH.
            if (request.method == Method.PATCH) {
                setRequestProperty("X-HTTP-Method-Override", Method.PATCH.value)
            }

            setDoOutput(connection, request.method)
            setBodyIfDoOutput(connection, request)
        }

        val headers = Headers.from(connection.headerFields)
        val transferEncoding = headers[Headers.TRANSFER_ENCODING].flatMap { it.split(',') }.map { it.trim() }
        val contentEncoding = headers[Headers.CONTENT_ENCODING].lastOrNull()
        var contentLength = headers[Headers.CONTENT_LENGTH].lastOrNull()?.toLong() ?: -1
        val shouldDecode = (request.decodeContent ?: decodeContent) && contentEncoding != null && contentEncoding != "identity"

        if (shouldDecode) {
            // `decodeContent` decodes the response, so the final response has no more `Content-Encoding`
            headers.remove(Headers.CONTENT_ENCODING)

            // URLConnection.getContentLength() returns the number of bytes transmitted and cannot be used to predict
            // how many bytes can be read from URLConnection.getInputStream() for compressed streams. Therefore if the
            // stream will be decoded, the length becomes unknown
            //
            headers.remove(Headers.CONTENT_LENGTH)

            contentLength = -1
        }

        // `decodeTransfer` decodes the response, so the final response has no more Transfer-Encoding
        headers.remove(Headers.TRANSFER_ENCODING)

        // [RFC 7230, 3.3.2](https://tools.ietf.org/html/rfc7230#section-3.3.2)
        //
        // When a message does not have a Transfer-Encoding header field, a
        //   Content-Length header field can provide the anticipated size, as a
        //   decimal number of octets, for a potential payload body.
        //
        //   A sender MUST NOT send a Content-Length header field in any message
        //   that contains a Transfer-Encoding header field.
        //
        // [RFC 7230, 3.3.3](https://tools.ietf.org/html/rfc7230#section-3.3.3)
        //
        // Any 2xx (Successful) response to a CONNECT request implies that
        //   the connection will become a tunnel immediately after the empty
        //   line that concludes the header fields.  A client MUST ignore any
        //   Content-Length or Transfer-Encoding header fields received in
        //   such a message.
        //
        if (transferEncoding.any { encoding -> encoding.isNotBlank() && encoding != "identity" }) {
            headers.remove(Headers.CONTENT_LENGTH)
            contentLength = -1
        }

        return Response(
            url = request.url,
            headers = headers,
            contentLength = contentLength,
            statusCode = connection.responseCode,
            responseMessage = connection.responseMessage.orEmpty(),
            body = DefaultBody.from(
                {
                    decodeContent(
                        stream = decodeTransfer(safeDataStream(connection), transferEncoding),
                        encoding = contentEncoding,
                        shouldDecode = shouldDecode
                    )
                },
                { contentLength }
            )
        )
    }

    private fun safeDataStream(connection: HttpURLConnection): InputStream? {
        return try {
            (connection.errorStream ?: connection.inputStream) ?.let { BufferedInputStream(it) }
        } catch (exception: IOException) {
            // Stream error
            try { (connection.errorStream ?: connection.inputStream).close() } catch (_: IOException) {}
            ByteArrayInputStream(exception.message?.toByteArray() ?: ByteArray(0))
        }
    }

    private fun decodeTransfer(stream: InputStream?, encodings: HeaderValues): InputStream {
        // No data stream
        if (stream == null) {
            return ByteArrayInputStream(ByteArray(0))
        }

        if (encodings.isEmpty()) {
            return stream
        }

        val encoding = encodings.first()
        return decodeTransfer(decode(stream, encoding), encodings.toList().drop(1))
    }

    private fun decodeContent(stream: InputStream, encoding: String?, shouldDecode: Boolean): InputStream {
        if (!shouldDecode || encoding.isNullOrBlank()) {
            return stream
        }

        return decode(stream, encoding)
    }

    private fun decode(stream: InputStream, encoding: String): InputStream {
        return when (encoding) {
            "gzip" -> GZIPInputStream(stream)
            "deflate" -> InflaterInputStream(stream)
            "identity" -> stream
            // the underlying HttpClient handles chunked, but does not remove the Transfer-Encoding Header
            "chunked" -> stream
            "" -> stream
            else -> throw UnsupportedOperationException("Decoding $encoding is not supported. $SUPPORTED_DECODING are.")
        }
    }

    private fun establishConnection(request: Request): URLConnection {
        val urlConnection = if (proxy != null) request.url.openConnection(proxy) else request.url.openConnection()
        return if (request.url.protocol == "https") {
            (urlConnection as HttpsURLConnection).apply {
                sslSocketFactory = request.socketFactory
                hostnameVerifier = request.hostnameVerifier
            }
        } else {
            urlConnection as HttpURLConnection
        }
    }

    private fun setBodyIfDoOutput(connection: HttpURLConnection, request: Request) {
        val body = request.body
        if (!connection.doOutput || body.isEmpty()) {
            return
        }

        val contentLength = body.length
        if (contentLength != null && contentLength != -1L) {
            // The content has a known length, so no need to chunk
            connection.setFixedLengthStreamingMode(contentLength.toLong())
        } else {
            // The content doesn't have a known length, so turn it into chunked
            connection.setChunkedStreamingMode(4096)
        }

        // The input and output streams returned by this class are not buffered. Most body implementations should wrap
        // the input stream with BufferedOutputStream. Bulk implementations may forgo this.
        body.writeTo(connection.outputStream)
        connection.outputStream.flush()
    }

    private fun setDoOutput(connection: HttpURLConnection, method: Method) = when (method) {
        Method.GET, Method.HEAD, Method.OPTIONS, Method.TRACE -> connection.doOutput = false
        Method.DELETE, Method.POST, Method.PUT, Method.PATCH -> connection.doOutput = true
    }

    companion object {
        val SUPPORTED_DECODING = listOf("gzip", "deflate; q=0.5")

        private fun coerceMethod(method: Method) = if (method == Method.PATCH) Method.POST else method
    }
}