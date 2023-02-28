package fuel

import okhttp3.Call
import okhttp3.Request.Builder
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.http.HttpMethod

public actual class HttpLoader(callFactory: Call.Factory) {
    private val fetcher: HttpUrlFetcher by lazy { HttpUrlFetcher(callFactory) }

    public actual suspend fun get(request: Request): HttpResponse {
        return fetcher.fetch(request, createRequestBuilder(request, "GET")).await()
    }

    public actual suspend fun post(request: Request): HttpResponse {
        requireNotNull(request.body) { "body for method POST should not be null" }
        return fetcher.fetch(request, createRequestBuilder(request, "POST")).await()
    }

    public actual suspend fun put(request: Request): HttpResponse {
        requireNotNull(request.body) { "body for method PUT should not be null" }
        return fetcher.fetch(request, createRequestBuilder(request, "PUT")).await()
    }

    public actual suspend fun patch(request: Request): HttpResponse {
        requireNotNull(request.body) { "body for method PATCH should not be null" }
        return fetcher.fetch(request, createRequestBuilder(request, "PATCH")).await()
    }

    public actual suspend fun delete(request: Request): HttpResponse {
        return fetcher.fetch(request, createRequestBuilder(request, "DELETE")).await()
    }

    public actual suspend fun head(request: Request): HttpResponse {
        return fetcher.fetch(request, createRequestBuilder(request, "HEAD")).await()
    }

    public actual suspend fun method(request: Request): HttpResponse {
        val method = requireNotNull(request.method) { "method should be not null" }
        return fetcher.fetch(request, createRequestBuilder(request, method)).await()
    }

    private fun createRequestBuilder(request: Request, method: String): Builder {
        val builder = Builder()
        with(builder) {
            request.headers?.forEach {
                addHeader(it.key, it.value)
            }

            if (HttpMethod.permitsRequestBody(method)) {
                method(method, request.body?.toRequestBody())
            } else {
                method(method, null)
            }
        }
        return builder
    }

    public companion object {
        public operator fun invoke(): HttpLoader = FuelBuilder().build()
    }
}
