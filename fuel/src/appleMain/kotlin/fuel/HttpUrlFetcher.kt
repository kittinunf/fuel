package fuel

import kotlin.collections.forEach
import kotlin.coroutines.resume
import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.io.Buffer
import platform.Foundation.NSData
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequestReloadIgnoringCacheData
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionConfiguration
import platform.Foundation.dataTaskWithRequest
import platform.Foundation.setHTTPBody
import platform.Foundation.setHTTPMethod
import platform.Foundation.setValue

internal class HttpUrlFetcher(private val sessionConfiguration: NSURLSessionConfiguration) {

    @OptIn(BetaInteropApi::class)
    suspend fun fetch(
        method: String,
        request: Request
    ): HttpResponse =
        suspendCancellableCoroutine { continuation ->
            val url =
                request.parameters?.let {
                    request.url.fillURLWithParameters(it)
                } ?: request.url

            val mutableURLRequest =
                NSMutableURLRequest.requestWithURL(NSURL(string = url)).apply {
                    request.body?.let {
                        setHTTPBody(it.encode())
                    }
                    request.headers.forEach {
                        setValue(it.value, forHTTPHeaderField = it.key)
                    }
                    setCachePolicy(NSURLRequestReloadIgnoringCacheData)
                    setHTTPMethod(method)
                }

            val session = NSURLSession.sessionWithConfiguration(sessionConfiguration)
            val task = session.dataTaskWithRequest(mutableURLRequest) { httpData, nsUrlResponse, error ->
                if (error != null) {
                    continuation.resumeWith(Result.failure(Throwable(error.localizedDescription)))
                    return@dataTaskWithRequest
                }

                val httpResponse = nsUrlResponse as? NSHTTPURLResponse
                if (httpResponse == null) {
                    continuation.resumeWith(Result.failure(Throwable("Invalid HTTP response")))
                    return@dataTaskWithRequest
                }

                continuation.resume(buildHttpResponse(httpData, httpResponse))
            }
            continuation.invokeOnCancellation { task.cancel() }
            task.resume()
        }

    fun fetchSSE(request: Request): Flow<String> = callbackFlow {
        val url =
            request.parameters?.let {
                request.url.fillURLWithParameters(it)
            } ?: request.url
        val mutableURLRequest =
            NSMutableURLRequest.requestWithURL(NSURL(string = url)).apply {
            setHTTPMethod("GET")
            setValue("text/event-stream", forHTTPHeaderField = "Accept")
            request.headers.forEach { setValue(it.value, forHTTPHeaderField = it.key) }
        }

        val session = NSURLSession.sessionWithConfiguration(sessionConfiguration)
        val task = session.dataTaskWithRequest(mutableURLRequest) { httpData, nsUrlResponse, error ->
            if (error != null) {
                close(Throwable(error.localizedDescription))
                return@dataTaskWithRequest
            }

            val httpResponse = nsUrlResponse as? NSHTTPURLResponse
            if (httpResponse?.statusCode?.toInt() != 200) {
                close(Throwable("Unexpected status code: ${httpResponse?.statusCode}"))
                return@dataTaskWithRequest
            }

            httpData?.toByteArray()?.decodeToString()?.lines()?.forEach { line ->
                if (line.startsWith("data: ")) {
                    trySend(line.removePrefix("data: ").trim()) // Send each event
                }
            }
        }

        awaitClose { task.cancel() } // Cancel task if flow is closed
        task.resume()
    }

    private fun buildHttpResponse(
        data: NSData?,
        httpResponse: NSHTTPURLResponse?,
    ): HttpResponse {
        if (httpResponse == null) {
            throw Throwable("Failed to parse HTTP network response: EOF")
        }

        val sourceBuffer = Buffer()
        sourceBuffer.write(data?.toByteArray() ?: ByteArray(0))

        return HttpResponse().apply {
            statusCode = httpResponse.statusCode.toInt()
            source = sourceBuffer
            headers = httpResponse.readHeaders()
        }
    }
}
