package fuel

import okhttp3.Call
import okhttp3.Request.Builder
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.executeAsync
import okhttp3.internal.http.HttpMethod

public class JVMHttpLoader(callFactoryLazy: Lazy<Call.Factory>) : HttpLoader {
    private val fetcher: HttpUrlFetcher by lazy { HttpUrlFetcher(callFactoryLazy) }

    public override suspend fun get(request: Request.Builder.() -> Unit): HttpResponse {
        val requestBuilder = Request.Builder().apply(request).build()
        return fetcher
            .fetch(requestBuilder, createRequestBuilder(requestBuilder, "GET"))
            .performAsync()
    }

    public override suspend fun post(request: Request.Builder.() -> Unit): HttpResponse {
        val requestBuilder = Request.Builder().apply(request).build()
        requireNotNull(requestBuilder.body) { "body for method POST should not be null" }
        return fetcher
            .fetch(requestBuilder, createRequestBuilder(requestBuilder, "POST"))
            .performAsync()
    }

    public override suspend fun put(request: Request.Builder.() -> Unit): HttpResponse {
        val requestBuilder = Request.Builder().apply(request).build()
        requireNotNull(requestBuilder.body) { "body for method PUT should not be null" }
        return fetcher
            .fetch(requestBuilder, createRequestBuilder(requestBuilder, "PUT"))
            .performAsync()
    }

    public override suspend fun patch(request: Request.Builder.() -> Unit): HttpResponse {
        val requestBuilder = Request.Builder().apply(request).build()
        requireNotNull(requestBuilder.body) { "body for method PATCH should not be null" }
        return fetcher
            .fetch(requestBuilder, createRequestBuilder(requestBuilder, "PATCH"))
            .performAsync()
    }

    public override suspend fun delete(request: Request.Builder.() -> Unit): HttpResponse {
        val requestBuilder = Request.Builder().apply(request).build()
        return fetcher
            .fetch(requestBuilder, createRequestBuilder(requestBuilder, "DELETE"))
            .performAsync()
    }

    public override suspend fun head(request: Request.Builder.() -> Unit): HttpResponse {
        val requestBuilder = Request.Builder().apply(request).build()
        return fetcher
            .fetch(requestBuilder, createRequestBuilder(requestBuilder, "HEAD"))
            .performAsync()
    }

    public override suspend fun method(request: Request.Builder.() -> Unit): HttpResponse {
        val requestBuilder = Request.Builder().apply(request).build()
        val method = requireNotNull(requestBuilder.method) { "method should be not null" }
        return fetcher
            .fetch(requestBuilder, createRequestBuilder(requestBuilder, method))
            .performAsync()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun Call.performAsync(): HttpResponse {
        val response = executeAsync()
        return HttpResponse().apply {
            statusCode = response.code
            body = response.body
            headers = response.toHeaders()
        }
    }

    private fun Response.toHeaders(): Map<String, String> {
        val header = mutableMapOf<String, String>()
        for ((key, values) in headers) {
            for (value in values) {
                header[key] = value.toString()
            }
        }
        return header
    }

    private fun createRequestBuilder(request: Request, method: String): Builder {
        val builder = Builder()
        with(builder) {
            request.headers.forEach {
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
