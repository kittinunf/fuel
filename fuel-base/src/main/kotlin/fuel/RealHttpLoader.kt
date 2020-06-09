// Inspired by https://github.com/coil-kt/coil/blob/master/coil-base/src/main/java/coil/RealImageLoader.kt

package fuel

import okhttp3.Call
import okhttp3.Request.Builder
import okhttp3.Response

internal class RealHttpLoader(callFactory: Call.Factory) : HttpLoader {
    private val fetcher = HttpUrlFetcher(callFactory)

    override suspend fun get(request: Request): Response {
        val requestBuilder = Builder().headers(request.headers)
        return fetcher.fetch(request.data, requestBuilder)
    }

    override suspend fun post(request: Request): Response {
        val requestBody = request.requestBody
            ?: throw IllegalArgumentException("RequestBody should not be null")
        val requestBuilder = Builder().headers(request.headers).post(requestBody)
        return fetcher.fetch(request.data, requestBuilder)
    }

    override suspend fun put(request: Request): Response {
        val requestBody = request.requestBody
            ?: throw IllegalArgumentException("RequestBody should not be null")
        val requestBuilder = Builder().headers(request.headers).put(requestBody)
        return fetcher.fetch(request.data, requestBuilder)
    }

    override suspend fun patch(request: Request): Response {
        val requestBody = request.requestBody
            ?: throw IllegalArgumentException("RequestBody should not be null")
        val requestBuilder = Builder().headers(request.headers).patch(requestBody)
        return fetcher.fetch(request.data, requestBuilder)
    }

    override suspend fun delete(request: Request): Response {
        val requestBuilder = Builder().headers(request.headers).delete(request.requestBody)
        return fetcher.fetch(request.data, requestBuilder)
    }

    override suspend fun head(request: Request): Response {
        val requestBuilder = Builder().headers(request.headers)
        return fetcher.fetch(request.data, requestBuilder)
    }

    override suspend fun method(request: Request): Response {
        val method = request.method ?: throw IllegalArgumentException("method should not be null")
        val requestBuilder = Builder().headers(request.headers).method(method, request.requestBody)
        return fetcher.fetch(request.data, requestBuilder)
    }
}
