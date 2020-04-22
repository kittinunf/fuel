// Inspired by https://github.com/coil-kt/coil/blob/master/coil-base/src/main/java/coil/ImageLoader.kt
@file:Suppress("FunctionName")

package fuel

import okhttp3.Response

interface HttpLoader {

    companion object {
        /** Alias to create an [HttpLoaderBuilder]. */
        fun Builder() = HttpLoaderBuilder()

        /** Alias to create a new [HttpLoader] without configuration */
        operator fun invoke() = HttpLoaderBuilder().build()
    }

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
}
