// Inspired by https://github.com/coil-kt/coil/blob/master/coil-base/src/main/java/coil/ImageLoader.kt
@file:Suppress("FunctionName", "MemberVisibilityCanBePrivate")

package fuel

import okhttp3.Call
import okhttp3.Response

public interface HttpLoader {

    /**
     * Wrap the [request]'s data by representing with [Call] which has been prepared for execution
     *
     * @param request The request to execute synchronously.
     * @return The [Response] result.
     */
    public fun get(request: Request): Call
    public fun post(request: Request): Call
    public fun put(request: Request): Call
    public fun patch(request: Request): Call
    public fun delete(request: Request): Call
    public fun head(request: Request): Call
    public fun method(request: Request): Call

    public companion object {
        public operator fun invoke(): HttpLoader = FuelBuilder().build()
    }
}
