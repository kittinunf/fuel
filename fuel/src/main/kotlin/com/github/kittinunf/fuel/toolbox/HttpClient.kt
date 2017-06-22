package com.github.kittinunf.fuel.toolbox

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.FuelError
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

class HttpClient(val proxy: Proxy? = null) : Client {
    override fun executeRequest(request: Request): Response {
        val response = Response()
        response.url = request.url

        val connection = establishConnection(request) as HttpURLConnection

        try {
            connection.apply {
                val timeout = Fuel.testConfiguration.timeout?.let { if (it == -1) Int.MAX_VALUE else it } ?: request.timeoutInMillisecond
                val timeoutRead = Fuel.testConfiguration.timeoutRead?.let { if (it == -1) Int.MAX_VALUE else it } ?: request.timeoutReadInMillisecond
                connectTimeout = timeout
                readTimeout = timeoutRead
                doInput = true
                useCaches = false
                requestMethod = if (request.httpMethod == Method.PATCH) Method.POST.value else request.httpMethod.value
                instanceFollowRedirects = false
                for ((key, value) in request.httpHeaders) {
                    setRequestProperty(key, value)
                }
                if (request.httpMethod == Method.PATCH) setRequestProperty("X-HTTP-Method-Override", "PATCH")
                setDoOutput(connection, request.httpMethod)
                setBodyIfDoOutput(connection, request)
            }

            return response.apply {

                httpResponseHeaders = connection.headerFields ?: emptyMap()
                httpContentLength = connection.contentLength.toLong()

                val contentEncoding = connection.contentEncoding ?: ""

                dataStream = try {
                    val stream = connection.errorStream ?: connection.inputStream
                    if (contentEncoding.compareTo("gzip", true) == 0) GZIPInputStream(stream) else stream
                } catch (exception: IOException) {
                    try {
                        connection.errorStream ?: connection.inputStream ?. close()
                    } catch (exception: IOException) { }
                    ByteArrayInputStream(kotlin.ByteArray(0))
                }

                //try - catch just in case both methods throw
                try {
                    httpStatusCode = connection.responseCode
                    httpResponseMessage = connection.responseMessage.orEmpty()
                } catch(exception: IOException) {
                    throw exception
                }
            }
        } catch(exception: Exception) {
            throw FuelError().apply {
                this.exception = exception
                this.errorData = response.data
                this.response = response
            }
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
            val contentLength = bodyCallback.invoke(request, null, 0)

            if (request.type == Request.Type.UPLOAD)
                connection.setFixedLengthStreamingMode(contentLength.toInt())

            val outStream = BufferedOutputStream(connection.outputStream)
            outStream.use {
                bodyCallback.invoke(request, outStream, contentLength)
            }
        }
    }

    private fun setDoOutput(connection: HttpURLConnection, method: Method) {
        when (method) {
            Method.GET, Method.DELETE, Method.HEAD -> connection.doOutput = false
            Method.POST, Method.PUT, Method.PATCH -> connection.doOutput = true
        }
    }
}


