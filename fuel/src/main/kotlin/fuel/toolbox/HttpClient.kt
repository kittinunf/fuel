package fuel.toolbox

import fuel.core.*
import fuel.util.build
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
        val connection = if (request.url.getProtocol().equals("https")) {
            val conn = request.url.openConnection() as HttpsURLConnection
            conn.setSSLSocketFactory(sslSocketFactory)
            conn.setHostnameVerifier { s, SSLSession -> true }
            conn
        } else {
            request.url.openConnection() as HttpURLConnection
        }

        val response = Response()
        response.url = request.url

        try {
            build(connection) {
                val timeout = request.timeoutInMillisecond
                setConnectTimeout(timeout)
                setReadTimeout(timeout)
                setDoInput(true)
                setUseCaches(false)
                setRequestMethod(request.httpMethod.value)
                setDoOutput(connection, request.httpMethod)
                for ((key, value) in request.httpHeaders) {
                    setRequestProperty(key, value)
                }
                setBodyIfAny(connection, request.httpBody)
            }

            return build(response) {

                httpResponseHeaders = connection.getHeaderFields()
                httpContentLength = connection.getContentLength().toLong()

                val contentEncoding = connection.getContentEncoding() ?: ""

                val dataStream = if (connection.getErrorStream() != null) {
                    connection.getErrorStream()
                } else {
                    connection.getInputStream()
                }

                data = if (contentEncoding.compareTo("gzip", true) == 0) {
                    GZIPInputStream(dataStream).readBytes()
                } else {
                    dataStream.readBytes()
                }

                //try - catch just in case both methods throw
                try {
                    httpStatusCode = connection.getResponseCode()
                    httpResponseMessage = connection.getResponseMessage()
                } catch(exception: IOException) {
                    throw build(FuelError()) {
                        this.exception = exception
                        this.response = response
                    }
                }
            }
        } catch(exception: Exception) {
            throw build(FuelError()) {
                this.exception = exception
                this.response = response
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun setBodyIfAny(connection: HttpURLConnection, bytes: ByteArray) {
        if (bytes.size() == 0) return

        val outStream = BufferedOutputStream(connection.getOutputStream());
        outStream.write(bytes);
        outStream.close();
    }

    private fun setDoOutput(connection: HttpURLConnection, method: Method) {
        when (method) {
            Method.GET, Method.DELETE -> connection.setDoOutput(false)
            Method.POST, Method.PUT -> connection.setDoOutput(true)
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

    return sslContext.getSocketFactory()
}
