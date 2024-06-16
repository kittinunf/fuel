package fuel

import kotlinx.browser.window
import kotlinx.coroutines.suspendCancellableCoroutine
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class HttpUrlFetcher {
    suspend fun fetch(request: Request, method: String?, body: String? = null): HttpResponse {
        val urlString = request.parameters?.let {
            request.url.fillURLWithParameters(it)
        } ?: request.url

        val requestInit = obj<RequestInit> {}
        requestInit.method = method
        requestInit.headers = request.headers.toJsReference()
        requestInit.body = body?.toJsString()
        return suspendCancellableCoroutine { continuation ->
            window.fetch(urlString, requestInit)
                .then {
                    if (it.ok) {
                        continuation.resume(
                            HttpResponse().apply {
                                statusCode = it.status
                                response = it
                                headers = it.headers.mapToFuel()
                            }
                        )
                        null
                    } else {
                        continuation.resumeWithException(Exception("Failed to fetch data: ${it.status}"))
                        null
                    }
                }
                .catch {
                    continuation.resumeWithException(Exception("Failed to fetch data: $it"))
                    null
                }
        }
    }

    private fun Headers.mapToFuel(): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        val keys = getKeys(this@mapToFuel)
        for (i in 0 until keys.length) {
            val key = keys[i].toString()
            val value = this@mapToFuel.get(key)!!
            headers[key] = value
        }
        return headers
    }
}
