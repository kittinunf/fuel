package fuel

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.get
import kotlinx.cinterop.reinterpret
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLAuthenticationChallenge
import platform.Foundation.NSURLCredential
import platform.Foundation.NSURLRequestReloadIgnoringCacheData
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionAuthChallengeDisposition
import platform.Foundation.NSURLSessionAuthChallengeUseCredential
import platform.Foundation.NSURLSessionConfiguration
import platform.Foundation.NSURLSessionDataDelegateProtocol
import platform.Foundation.NSURLSessionDataTask
import platform.Foundation.NSURLSessionTask
import platform.Foundation.NSWindowsCP1251StringEncoding
import platform.Foundation.create
import platform.Foundation.credentialForTrust
import platform.Foundation.dataUsingEncoding
import platform.Foundation.serverTrust
import platform.Foundation.setHTTPBody
import platform.Foundation.setHTTPMethod
import platform.Foundation.setValue
import platform.darwin.NSObject
import kotlin.native.concurrent.freeze

internal class HttpUrlFetcher(private val sessionConfiguration: NSURLSessionConfiguration) :
    NSObject(), NSURLSessionDataDelegateProtocol {

    private val asyncQueue = NSOperationQueue()
    private val chunks = Channel<ByteArray>(Channel.UNLIMITED)
    private val rawResponse = CompletableDeferred<NSHTTPURLResponse>()

    init {
        freeze()
    }

    suspend fun fetch(method: String, request: Request): HttpResponse {
        val mutableURLRequest = NSMutableURLRequest.requestWithURL(NSURL(string = request.url)).apply {
            request.body?.let {
                setHTTPBody(it.encode())
            }
            request.headers?.forEach {
                setValue(it.value, it.key)
            }
            setCachePolicy(NSURLRequestReloadIgnoringCacheData)
            setHTTPMethod(method)
        }

        val session = NSURLSession.sessionWithConfiguration(sessionConfiguration, this, asyncQueue)
        session.dataTaskWithRequest(mutableURLRequest).resume()

        var result = chunks.receive()
        chunks.consumeEach {
            result += it
        }

        return HttpResponse().apply {
            statusCode = rawResponse.await().statusCode.toInt()
            body = result.decodeToString()
        }
    }

    override fun URLSession(session: NSURLSession, dataTask: NSURLSessionDataTask, didReceiveData: NSData) {
        initRuntimeIfNeeded()

        if (!rawResponse.isCompleted) {
            val response = dataTask.response as NSHTTPURLResponse
            rawResponse.complete(response)
        }

        val content = didReceiveData.toByteArray()
        chunks.trySend(content)
    }

    override fun URLSession(session: NSURLSession, task: NSURLSessionTask, didCompleteWithError: NSError?) {
        initRuntimeIfNeeded()
        didCompleteWithError?.let {
            chunks.close(Throwable(it.localizedDescription))
        }

        if (!rawResponse.isCompleted) {
            val response = task.response as NSHTTPURLResponse
            rawResponse.complete(response)
        }

        chunks.close()
    }

    override fun URLSession(
        session: NSURLSession,
        didReceiveChallenge: NSURLAuthenticationChallenge,
        completionHandler: (NSURLSessionAuthChallengeDisposition, NSURLCredential?) -> Unit
    ) {
        val serverTrust = didReceiveChallenge.protectionSpace.serverTrust
        if (serverTrust == null) {
            completionHandler(NSURLSessionAuthChallengeUseCredential, null)
        } else {
            completionHandler(NSURLSessionAuthChallengeUseCredential, NSURLCredential.credentialForTrust(serverTrust))
        }
    }

    private fun NSData.toByteArray(): ByteArray {
        val data: CPointer<ByteVar> = bytes!!.reinterpret()
        return ByteArray(length.toInt()) { index -> data[index] }
    }

    private fun String.encode(): NSData = NSString.create(string = this).dataUsingEncoding(NSWindowsCP1251StringEncoding)!!
}
