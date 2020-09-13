// Inspired by https://github.com/coil-kt/coil/blob/master/coil-base/src/main/java/coil/ImageLoader.kt
@file:Suppress("FunctionName")

package fuel

import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Response

public interface HttpLoader {
    /**
     * Load the [request]'s data and suspend until the operation is complete. Return the loaded [Response].
     *
     * @param request The request to execute.
     * @return The [Response] result.
     */
    public suspend fun get(request: Request): Response
    public suspend fun post(request: Request): Response
    public suspend fun put(request: Request): Response
    public suspend fun patch(request: Request): Response
    public suspend fun delete(request: Request): Response
    public suspend fun head(request: Request): Response
    public suspend fun method(request: Request): Response

    public class Builder {
        private var callFactory: Call.Factory? = null

        /**
         * Set the [OkHttpClient] used for network requests.
         *
         * This is a convenience function for calling `callFactory(Call.Factory)`.
         */
        public fun okHttpClient(okHttpClient: OkHttpClient): Builder = callFactory(okHttpClient)

        /**
         * Set a lazy callback to create the [OkHttpClient] used for network requests.
         *
         * This is a convenience function for calling `callFactory(() -> Call.Factory)`.
         */
        public fun okHttpClient(initializer: () -> OkHttpClient): Builder = callFactory(initializer)

        /**
         * Set the [Call.Factory] used for network requests.
         *
         * Calling [okHttpClient] automatically sets this value.
         */
        public fun callFactory(callFactory: Call.Factory): Builder = apply {
            this.callFactory = callFactory
        }

        /**
         * Set a lazy callback to create the [Call.Factory] used for network requests.
         *
         * This allows lazy creation of the [Call.Factory] on a background thread.
         * [initializer] is guaranteed to be called at most once.
         *
         * Prefer using this instead of `callFactory(Call.Factory)`.
         *
         * Calling [okHttpClient] automatically sets this value.
         */
        public fun callFactory(initializer: () -> Call.Factory): Builder = apply {
            this.callFactory = lazyCallFactory(initializer)
        }

        /**
         * Create a new [HttpLoader] instance.
         */
        public fun build(): HttpLoader = RealHttpLoader(callFactory ?: buildDefaultCallFactory())

        private fun buildDefaultCallFactory() = lazyCallFactory {
            OkHttpClient.Builder().build()
        }
    }

    public companion object {
        /** Alias to create a new [HttpLoader] without configuration */
        public operator fun invoke(): HttpLoader = Builder().build()
    }
}
