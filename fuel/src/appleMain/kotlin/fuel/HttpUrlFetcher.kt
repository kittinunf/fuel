package fuel

import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.suspendCancellableCoroutine
import okio.ByteString.Companion.toByteString
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequestReloadIgnoringCacheData
import platform.Foundation.NSURLResponse
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionConfiguration
import platform.Foundation.NSWindowsCP1251StringEncoding
import platform.Foundation.create
import platform.Foundation.dataTaskWithRequest
import platform.Foundation.dataUsingEncoding
import platform.Foundation.setHTTPBody
import platform.Foundation.setHTTPMethod
import platform.Foundation.setValue
import kotlin.coroutines.resume

internal class HttpUrlFetcher(private val sessionConfiguration: NSURLSessionConfiguration) {
    @OptIn(BetaInteropApi::class)
    suspend fun fetch(method: String, request: Request): HttpResponse = suspendCancellableCoroutine { continuation ->
        val delegate = { httpData: NSData?, nsUrlResponse: NSURLResponse?, error: NSError? ->
            continuation.resume(
                buildHttpResponse(
                    data = httpData,
                    httpResponse = nsUrlResponse as? NSHTTPURLResponse,
                    error = error
                )
            )
        }

        val url = request.parameters?.let {
            request.url.fillURLWithParameters(it)
        } ?: request.url

        val mutableURLRequest = NSMutableURLRequest.requestWithURL(NSURL(string = url)).apply {
            request.body?.let {
                setHTTPBody(it.encode())
            }
            request.headers.forEach {
                setValue(it.value, it.key)
            }
            setCachePolicy(NSURLRequestReloadIgnoringCacheData)
            setHTTPMethod(method)
        }

        val session = NSURLSession.sessionWithConfiguration(sessionConfiguration)
        val task = session.dataTaskWithRequest(mutableURLRequest, delegate)
        continuation.invokeOnCancellation {
            task.cancel()
        }
        task.resume()
    }

    private fun buildHttpResponse(
        data: NSData?,
        httpResponse: NSHTTPURLResponse?,
        error: NSError?
    ): HttpResponse {
        if (error != null) {
            throw Throwable(error.localizedDescription)
        }

        if (httpResponse == null) {
            throw Throwable("Failed to parse http network response: EOF")
        }

        /**
         * data can be empty if there is no body.
         * In that case, trying to create a ByteString fails
         */
        val bodyString = if (data == null || data.length.toInt() == 0) {
            null
        } else {
            data.toByteString().utf8()
        }

        return HttpResponse().apply {
            statusCode = httpResponse.statusCode.toInt()
            nsData = data
            body = bodyString
        }
    }

    @BetaInteropApi
    private fun String.encode(): NSData = NSString.create(string = this).dataUsingEncoding(NSWindowsCP1251StringEncoding)!!
}
