package fuel.toolbox

import fuel.core.Client
import fuel.core.FuelError
import fuel.core.Request
import fuel.core.Response
import fuel.util.build
import java.net.HttpURLConnection

/**
 * Created by Kittinun Vantasin on 5/15/15.
 */

class HttpClient : Client {

    override fun executeRequest(request: Request): Response {
        val url = request.url

        val connection = url.openConnection() as HttpURLConnection
        build(connection) {
            val timeout = request.timeoutInMillisecond
            setConnectTimeout(timeout)
            setReadTimeout(timeout)
            setDoInput(true)
            setRequestMethod(request.httpMethod.value)
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

}