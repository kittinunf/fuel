package com.github.kittinunf.fuel.toolbox

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.Proxy
import java.net.URLConnection
import java.util.zip.GZIPInputStream
import javax.net.ssl.HttpsURLConnection
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class HttpClient(private val proxy: Proxy? = null) : Client {
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
            doInput = true
            useCaches = false
            requestMethod = if (request.method == Method.PATCH) Method.POST.value else request.method.value
            instanceFollowRedirects = false

            request.headers.transformIterate(
                { key, values -> setRequestProperty(key, values) },
                { key, value -> addRequestProperty(key, value) }
            )

            if (request.method == Method.PATCH) {
                setRequestProperty("X-HTTP-Method-Override", "PATCH")
            }

            setDoOutput(connection, request.method)
            setBodyIfDoOutput(connection, request)
        }

        val contentEncoding = connection.contentEncoding ?: ""
        return Response(
                url = request.url,
                headers = Headers.from(connection.headerFields),
                contentLength = connection.contentLength.toLong(),
                statusCode = connection.responseCode,
                responseMessage = connection.responseMessage.orEmpty(),
                dataStream = try {
                    val stream = connection.errorStream ?: connection.inputStream
                    if (contentEncoding.compareTo("gzip", true) == 0) GZIPInputStream(stream) else stream
                } catch (exception: IOException) {
                    try {
                        connection.errorStream ?: connection.inputStream?.close()
                    } catch (exception: IOException) {
                    }
                    ByteArrayInputStream(ByteArray(0))
                }
        )
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
        val bodyCallback = request.bodyCallback
        if (bodyCallback != null && connection.doOutput) {
            val contentLength = bodyCallback(request, null, 0)

            if (request.type == Request.Type.UPLOAD)
                connection.setFixedLengthStreamingMode(contentLength.toInt())

            BufferedOutputStream(connection.outputStream).use {
                bodyCallback(request, it, contentLength)
            }
        }
    }

    private fun setDoOutput(connection: HttpURLConnection, method: Method) = when (method) {
        Method.GET, Method.HEAD, Method.OPTIONS, Method.TRACE -> connection.doOutput = false
        Method.DELETE, Method.POST, Method.PUT, Method.PATCH -> connection.doOutput = true
    }
}