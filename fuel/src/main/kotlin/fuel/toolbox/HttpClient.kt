package fuel.toolbox

import fuel.core.Client
import fuel.core.FuelError
import fuel.core.Request
import fuel.core.Response
import fuel.util.build
import java.io.DataOutputStream
import java.io.ObjectOutputStream
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
            setDoOutput(true)
            setRequestMethod(request.httpMethod.value)
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

}