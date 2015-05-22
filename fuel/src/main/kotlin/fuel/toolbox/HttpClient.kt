package fuel.toolbox

import fuel.core.*
import fuel.util.build
import java.io.DataOutputStream
import java.net.HttpURLConnection

/**
 * Created by Kittinun Vantasin on 5/15/15.
 */

class HttpClient : Client {

    override fun executeRequest(request: Request): Response {
        val connection = request.url.openConnection() as HttpURLConnection

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

        val response = Response()
        return build(response) {
            try {
                httpStatusCode = connection.getResponseCode()
                httpResponseMessage = connection.getResponseMessage()
                httpResponseHeaders = connection.getHeaderFields()
                dataStream = if (connection.getErrorStream() != null) connection.getErrorStream() else connection.getInputStream()
            } catch(exception: Exception) {
                throw build(FuelError()) {
                    this.exception = exception
                    this.response = response
                }
            }
        }
    }

    private fun setBodyIfAny(connection: HttpURLConnection, bytes: ByteArray?) {
        if (bytes == null) return

        val outStream = DataOutputStream(connection.getOutputStream());
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