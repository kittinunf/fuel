package fuel.toolbox

import fuel.core.Client
import fuel.core.Request
import fuel.core.Response
import fuel.util.HttpURLConnection
import java.io.BufferedInputStream

/**
 * Created by Kittinun Vantasin on 5/15/15.
 */

class HttpClient : Client {

    override fun executeRequest(request: Request): Response {
        val url = request.url

        val connection = HttpURLConnection(url) {
            val timeout = request.timeoutInMillisecond
            setConnectTimeout(timeout)
            setReadTimeout(timeout)
            setDoInput(true)
            setRequestMethod(request.method.value)
        }

        val response = Response {
            httpStatusCode = connection.getResponseCode()
            dataStream = connection.getInputStream()
        }

        return response
    }

}