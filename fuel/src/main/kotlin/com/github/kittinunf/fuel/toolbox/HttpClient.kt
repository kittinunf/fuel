package com.github.kittinunf.fuel.toolbox

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.*
import java.io.BufferedOutputStream
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
                requestMethod = request.httpMethod.value
                setDoOutput(connection, request.httpMethod)
                instanceFollowRedirects = false
                for ((key, value) in request.httpHeaders) {
                    setRequestProperty(key, value)
                }
                setBodyIfAny(connection, request.httpBody)
            }

            return response.apply {

                httpResponseHeaders = connection.headerFields ?: emptyMap()
                httpContentLength = connection.contentLength.toLong()

                val contentEncoding = connection.contentEncoding ?: ""

                val dataStream = if (connection.errorStream != null) {
                    connection.errorStream
                } else {
                    try {
                        connection.inputStream
                    } catch(exception: IOException) {
                        null
                    }
                }

                if (dataStream != null) {
                    data = if (contentEncoding.compareTo("gzip", true) == 0) {
                        GZIPInputStream(dataStream).readBytes()
                    } else {
                        dataStream.readBytes()
                    }
                }

                //try - catch just in case both methods throw
                try {
                    httpStatusCode = connection.responseCode
                    httpResponseMessage = connection.responseMessage
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
            connection.disconnect()
        }
    }

    private fun establishConnection(request: Request): URLConnection {
        val urlConnection = if (proxy != null) request.url.openConnection(proxy) else request.url.openConnection()
        return if (request.url.protocol.equals("https")) {
            val conn = urlConnection as HttpsURLConnection
            conn.apply {
                sslSocketFactory = request.socketFactory
                hostnameVerifier = request.hostnameVerifier
            }
        } else {
            urlConnection as HttpURLConnection
        }
    }

    private fun setBodyIfAny(connection: HttpURLConnection, bytes: ByteArray) {
        if (bytes.size != 0) {
            val outStream = BufferedOutputStream(connection.outputStream)
            outStream.write(bytes)
            outStream.close()
        }
    }

    private fun setDoOutput(connection: HttpURLConnection, method: Method) {
        when (method) {
            Method.GET, Method.DELETE, Method.HEAD -> connection.doOutput = false
            Method.POST, Method.PUT, Method.PATCH -> connection.doOutput = true
        }
    }
}


