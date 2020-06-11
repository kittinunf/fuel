// Inspired by https://github.com/coil-kt/coil/blob/master/coil-base/src/main/java/coil/ImageLoader.kt
@file:Suppress("FunctionName")

package fuel

import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Response

interface HttpLoader {
    /**
     * Load the [request]'s data and suspend until the operation is complete. Return the loaded [Response].
     *
     * @param request The request to execute.
     * @return The [Response] result.
     */
    suspend fun get(request: Request): Response
    suspend fun post(request: Request): Response
    suspend fun put(request: Request): Response
    suspend fun patch(request: Request): Response
    suspend fun delete(request: Request): Response
    suspend fun head(request: Request): Response
    suspend fun method(request: Request): Response

    class Builder {
        private var callFactory: Call.Factory? = null

        /**
         * Set the [OkHttpClient] used for network requests.
         *
         * This is a convenience function for calling `callFactory(Call.Factory)`.
         */
        fun okHttpClient(okHttpClient: OkHttpClient) = callFactory(okHttpClient)

        /**
         * Set a lazy callback to create the [OkHttpClient] used for network requests.
         *
         * This is a convenience function for calling `callFactory(() -> Call.Factory)`.
         */
        fun okHttpClient(initializer: () -> OkHttpClient) = callFactory(initializer)

        /**
         * Set the [Call.Factory] used for network requests.
         *
         * Calling [okHttpClient] automatically sets this value.
         */
        fun callFactory(callFactory: Call.Factory) = apply {
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
        fun callFactory(initializer: () -> Call.Factory) = apply {
            this.callFactory = lazyCallFactory(initializer)
        }

        /**
         * Create a new [HttpLoader] instance.
         */
        fun build(): HttpLoader = RealHttpLoader(callFactory ?: buildDefaultCallFactory())

        private fun buildDefaultCallFactory() = lazyCallFactory {
            OkHttpClient.Builder().build()
        }
    }

    companion object {
        /** Alias to create a new [HttpLoader] without configuration */
        operator fun invoke() = Builder().build()
    }
}
