package fuel

import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.fetch.RequestInit

internal class HttpUrlFetcher {
    suspend fun fetch(urlString: String, method: String?, body: String? = null): HttpResponse {
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
