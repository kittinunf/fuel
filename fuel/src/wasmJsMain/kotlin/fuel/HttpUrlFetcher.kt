package fuel

import kotlinx.browser.window
import kotlinx.coroutines.suspendCancellableCoroutine
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
        requestInit.headers = request.headers?.toJsReference()
        requestInit.body = body?.toJsString()
        return suspendCancellableCoroutine { continuation ->
            window.fetch(urlString, requestInit)
                .then {
                    if (it.ok) {
                        continuation.resume( HttpResponse().apply {
                            statusCode = it.status
                            response = it
                        } )
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
}
