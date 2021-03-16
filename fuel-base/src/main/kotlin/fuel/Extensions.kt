// Inspired by https://github.com/coil-kt/coil/blob/master/coil-base/src/main/java/coil/util/Extensions.kt

package fuel

import okhttp3.Call
import okhttp3.Headers
import okhttp3.Response

/**
 * Call response extension that allows to execute the Call with validator
 *
 * @return Response of response with validator by default it uses [Response.isSuccessful] to determine whether response
 * is successful or not, otherwise throw exception
 */
public fun Call.response(validator: Response.() -> Response = Response::validate): Response = execute().validator()

/**
 * Wrap a [Call.Factory] factory as a [Call.Factory] instance.
 * [initializer] is called only once the first time [Call.Factory.newCall] is called.
 */
internal fun lazyCallFactory(initializer: () -> Call.Factory): Call.Factory {
    val lazy: Lazy<Call.Factory> = lazy(initializer)
    return Call.Factory { request -> lazy.value.newCall(request) }
}

private val EMPTY_HEADERS = Headers.Builder().build()

internal fun Headers?.orEmpty() = this ?: EMPTY_HEADERS
