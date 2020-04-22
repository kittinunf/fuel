// Copied by https://github.com/coil-kt/coil/blob/master/coil-base/src/main/java/coil/util/ContinuationCallback.kt

package fuel

import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CompletionHandler
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response

/** @see [Call.await]. */
internal class ContinuationCallback(
    private val call: Call,
    private val continuation: CancellableContinuation<Response>
) : Callback, CompletionHandler {
    override fun onResponse(call: Call, response: Response) {
        continuation.resume(response)
    }

    override fun onFailure(call: Call, e: IOException) {
        if (!call.isCanceled()) {
            continuation.resumeWithException(e)
        }
    }

    override fun invoke(cause: Throwable?) {
        try {
            call.cancel()
        } catch (_: Throwable) {}
    }
}
