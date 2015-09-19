package fuel.toolbox

import fuel.core.*
import java.io.BufferedOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.zip.GZIPInputStream
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

/**
 * Created by Kittinun Vantasin on 5/15/15.
 */

class HttpClient(val sslSocketFactory: SSLSocketFactory = defaultSocketFactory()) : Client {

    override fun executeRequest(request: Request): Response {
        val connection = if (request.url.protocol.equals("https")) {
            val conn = request.url.openConnection() as HttpsURLConnection
            conn.sslSocketFactory = sslSocketFactory
            conn.setHostnameVerifier { s, SSLSession -> true }
            conn
        } else {
            request.url.openConnection() as HttpURLConnection
        }

        val response = Response()
        response.url = request.url

        try {
            connection.apply {
                val timeout = request.timeoutInMillisecond
                connectTimeout = timeout
                readTimeout = timeout
                doInput = true
                useCaches = false
                requestMethod = request.httpMethod.value
                setDoOutput(connection, request.httpMethod)
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
                    try { connection.inputStream
                    } catch(exception: IOException) { null }
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
                    throw FuelError().apply {
                        this.exception = exception
                        this.errorData = response.data
                    }
                }
            }
        } catch(exception: Exception) {
            throw FuelError().apply {
                this.exception = exception
                this.errorData = response.data
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun setBodyIfAny(connection: HttpURLConnection, bytes: ByteArray) {
        if (bytes.size() == 0) return

        val outStream = BufferedOutputStream(connection.outputStream);
        outStream.write(bytes);
        outStream.close();
    }

    private fun setDoOutput(connection: HttpURLConnection, method: Method) {
        when (method) {
            Method.GET, Method.DELETE -> connection.doOutput = false
            Method.POST, Method.PUT, Method.PATCH -> connection.doOutput = true
        }
    }

}

fun defaultSocketFactory(): SSLSocketFactory {
    val trustAllCerts = arrayOf(object : X509TrustManager {

        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String) {
        }

        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String) {
        }

        override fun getAcceptedIssuers(): Array<out X509Certificate>? {
            return null
        }

    })

    val sslContext = SSLContext.getInstance("SSL")
    sslContext.init(null, trustAllCerts, SecureRandom())

    return sslContext.socketFactory
}
