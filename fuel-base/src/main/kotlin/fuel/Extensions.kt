// Inspired by https://github.com/coil-kt/coil/blob/master/coil-base/src/main/java/coil/util/Extensions.kt

package fuel

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Headers
import okhttp3.Request
import okhttp3.Response

internal suspend inline fun Call.await(): Response = suspendCancellableCoroutine { continuation ->
    val callback = ContinuationCallback(this, continuation)
    enqueue(callback)
    continuation.invokeOnCancellation(callback)
}

/**
 * Wrap a [Call.Factory] factory as a [Call.Factory] instance.
 * [initializer] is called only once the first time [Call.Factory.newCall] is called.
 */
internal fun lazyCallFactory(initializer: () -> Call.Factory): Call.Factory {
    val lazy: Lazy<Call.Factory> = lazy(initializer)
    return object : Call.Factory {
        override fun newCall(request: Request) = lazy.value.newCall(request)
    }
}

private val EMPTY_HEADERS = Headers.Builder().build()

internal fun Headers?.orEmpty() = this ?: EMPTY_HEADERS
