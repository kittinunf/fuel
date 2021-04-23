// Inspired by https://github.com/coil-kt/coil/blob/master/coil-base/src/main/java/coil/RealImageLoader.kt

package fuel

import okhttp3.Call
import okhttp3.Request.Builder
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.internal.http.HttpMethod

internal class RealHttpLoader(callFactory: Call.Factory) : HttpLoader {

    private val fetcher by lazy { HttpUrlFetcher(callFactory) }

    override suspend fun get(request: Request): Response =
        fetcher.fetch(request.url, createRequestBuilder(request, "GET")).await()

    override suspend fun post(request: Request): Response {
        requireNotNull(request.body) { "body for method POST should not be null" }
        return fetcher.fetch(request.url, createRequestBuilder(request, "POST")).await()
    }

    override suspend fun put(request: Request): Response {
        requireNotNull(request.body) { "body for method PUT should not be null" }
        return fetcher.fetch(request.url, createRequestBuilder(request, "PUT")).await()
    }

    override suspend fun patch(request: Request): Response {
        requireNotNull(request.body) { "body for method PATCH should not be null" }
        return fetcher.fetch(request.url, createRequestBuilder(request, "PATCH")).await()
    }

    override suspend fun delete(request: Request): Response =
        fetcher.fetch(request.url, createRequestBuilder(request, "DELETE")).await()

    override suspend fun head(request: Request): Response =
        fetcher.fetch(request.url, createRequestBuilder(request, "HEAD")).await()

    override suspend fun method(request: Request): Response {
        val method = requireNotNull(request.method) { "method should be not null" }
        return fetcher.fetch(request.url, createRequestBuilder(request, method)).await()
    }

    private fun createRequestBuilder(request: Request, method: String): Builder {
        val builder = Builder()
        with(builder) {
            request.headers?.forEach {
                addHeader(it.key, it.value)
            }

            if (HttpMethod.permitsRequestBody(method))
                method(method, request.body?.toRequestBody())
            else
                method(method, null)
        }
        return builder
    }
}
