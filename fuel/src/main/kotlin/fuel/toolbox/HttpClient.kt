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
            setMethod(connection, request.httpMethod)
            for ((key, value) in request.httpHeaders) {
                setRequestProperty(key, value)
            }
            setBodyIfAny(connection, request.httpBody)
        }

        val response = Response()
        return build(response) {
            httpStatusCode = connection.getResponseCode()
            httpResponseMessage = connection.getResponseMessage()
            try {
                dataStream = connection.getInputStream()
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

    private fun setMethod(connection: HttpURLConnection, method: Method) {
        when(method) {
            is Method.DELETE -> {
                //fix known bug of httpURLConnection when use DELETE method and setDoOutput(true)
                //http://bugs.java.com/view_bug.do?bug_id=7157360
                connection.setDoOutput(false)
            }
            else -> {
                connection.setDoOutput(true)
            }
        }
        connection.setRequestMethod(method.value)
    }

}