package fuel

import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit

internal class HttpUrlFetcher {
    suspend fun fetch(request: Request, method: String?, body: String? = null): HttpResponse {
        val urlString = request.parameters?.let {
            request.url.fillURLWithParameters(it)
        } ?: request.url

        val res = window.fetch(
            urlString,
            object : RequestInit {
                override var method: String? = method
                override var body: dynamic = body.asDynamic()
            }
        ).await()
        return HttpResponse().apply {
            this.statusCode = res.status.toInt()
            this.response = res
            this.headers = res.headers.mapToFuel()
        }
    }

    private fun Headers.mapToFuel(): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        this@mapToFuel.asDynamic().forEach { value: String, key: String ->
            headers.put(key, value)
        }
        return headers
    }
}
