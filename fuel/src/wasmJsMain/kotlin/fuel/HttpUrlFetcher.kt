package fuel

import kotlinx.browser.window
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.io.Buffer
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class HttpUrlFetcher {
    suspend fun fetch(
        request: Request,
        method: String?,
        body: String? = null,
    ): HttpResponse {
        val urlString =
            request.parameters?.let {
                request.url.fillURLWithParameters(it)
            } ?: request.url

        val requestInit = obj<RequestInit> {}
        requestInit.method = method
        requestInit.headers = request.headers.toJsReference()
        requestInit.body = body?.toJsString()
        return suspendCancellableCoroutine { continuation ->
            window.fetch(urlString, requestInit)
                .then { response ->
                    if (response.ok) {
                        response.arrayBuffer()
                            .then { arrayBuffer ->
                                val byteArray = arrayBuffer.toBuffer()
                                continuation.resume(
                                    HttpResponse().apply {
                                        statusCode = response.status.toInt()
                                        source = byteArray
                                        headers = response.headers.mapToFuel()
                                    },
                                )
                                null
                            }
                        null
                    } else {
                        continuation.resumeWithException(Exception("Failed to fetch data: ${response.status}"))
                        null
                    }
                }
                .catch {
                    continuation.resumeWithException(Exception("Failed to fetch data: $it"))
                    null
                }
        }
    }

    private fun ArrayBuffer.toBuffer(): Buffer {
        val uint8Array = Uint8Array(this)
        val buffer = Buffer()
        for (i in 0 until uint8Array.length) {
            buffer.writeByte(uint8Array[i].toInt().toByte())
        }
        return buffer
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
