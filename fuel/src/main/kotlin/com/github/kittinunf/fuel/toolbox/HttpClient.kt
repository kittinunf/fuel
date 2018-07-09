package com.github.kittinunf.fuel.toolbox

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.FuelError
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.Proxy
import java.net.URLConnection
import java.util.zip.GZIPInputStream
import javax.net.ssl.HttpsURLConnection

internal class HttpClient(private val proxy: Proxy? = null) : Client {
    override fun executeRequest(request: Request): Response {
        try {
            val connection = establishConnection(request) as HttpURLConnection
            connection.apply {
                connectTimeout = Fuel.testConfiguration.coerceTimeout(request.timeoutInMillisecond)
                readTimeout = Fuel.testConfiguration.coerceTimeoutRead(request.timeoutReadInMillisecond)
                doInput = true
                useCaches = false
                requestMethod = if (request.method == Method.PATCH) Method.POST.value else request.method.value
                instanceFollowRedirects = false

                for ((key, value) in request.headers) {
                    setRequestProperty(key, value)
                }

                if (request.method == Method.PATCH) {
                    setRequestProperty("X-HTTP-Method-Override", "PATCH")
                }

                setDoOutput(connection, request.method)
                setBodyIfDoOutput(connection, request)
            }

            val contentEncoding = connection.contentEncoding ?: ""

            return Response(
                    url = request.url,
                    headers = connection.headerFields.filterKeys { it != null },
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
        } catch (exception: Exception) {
            throw FuelError(exception, ByteArray(0), Response(request.url))
        } finally {
            //As per Android documentation, a connection that is not explicitly disconnected
            //will be pooled and reused!  So, don't close it as we need inputStream later!
            //connection.disconnect()
        }
    }

    private fun establishConnection(request: Request): URLConnection {
        val urlConnection = if (proxy != null) request.url.openConnection(proxy) else request.url.openConnection()
        return if (request.url.protocol == "https") {
            val conn = urlConnection as HttpsURLConnection
            conn.apply {
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
        Method.GET, Method.DELETE, Method.HEAD -> connection.doOutput = false
        Method.POST, Method.PUT, Method.PATCH -> connection.doOutput = true
    }
}


