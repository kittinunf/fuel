package fuel

import kotlinx.browser.window
import kotlinx.coroutines.await
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
            this.body = res.text().await()
        }
    }
}
