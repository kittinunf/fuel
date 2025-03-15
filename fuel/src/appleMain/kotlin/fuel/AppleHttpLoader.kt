package fuel

import kotlinx.coroutines.flow.Flow
import platform.Foundation.NSURLSessionConfiguration

public class AppleHttpLoader(
    sessionConfiguration: NSURLSessionConfiguration,
) : HttpLoader {
    private val fetcher by lazy { HttpUrlFetcher(sessionConfiguration) }

    public override suspend fun get(request: Request.Builder.() -> Unit): HttpResponse {
        val requestBuilder = Request.Builder().apply(request).build()
        return fetcher.fetch("GET", requestBuilder)
    }

    public override suspend fun post(request: Request.Builder.() -> Unit): HttpResponse {
        val requestBuilder = Request.Builder().apply(request).build()
        requireNotNull(requestBuilder.body) { "body for method POST should not be null" }
        return fetcher.fetch("POST", requestBuilder)
    }

    public override suspend fun put(request: Request.Builder.() -> Unit): HttpResponse {
        val requestBuilder = Request.Builder().apply(request).build()
        requireNotNull(requestBuilder.body) { "body for method PUT should not be null" }
        return fetcher.fetch("PUT", requestBuilder)
    }

    public override suspend fun patch(request: Request.Builder.() -> Unit): HttpResponse {
        val requestBuilder = Request.Builder().apply(request).build()
        requireNotNull(requestBuilder.body) { "body for method PATCH should not be null" }
        return fetcher.fetch("PATCH", requestBuilder)
    }

    public override suspend fun delete(request: Request.Builder.() -> Unit): HttpResponse {
        val requestBuilder = Request.Builder().apply(request).build()
        return fetcher.fetch("DELETE", requestBuilder)
    }

    public override suspend fun head(request: Request.Builder.() -> Unit): HttpResponse {
        val requestBuilder = Request.Builder().apply(request).build()
        return fetcher.fetch("HEAD", requestBuilder)
    }

    override suspend fun sse(request: Request.Builder.() -> Unit): Flow<String> {
        val requestBuilder = Request.Builder().apply(request).build()
        return fetcher.fetchSSE(requestBuilder)
    }

    public override suspend fun method(request: Request.Builder.() -> Unit): HttpResponse {
        val requestBuilder = Request.Builder().apply(request).build()
        val method = requireNotNull(requestBuilder.method) { "method should be not null" }
        return fetcher.fetch(method, requestBuilder)
    }

    public companion object {
        public operator fun invoke(): HttpLoader = FuelBuilder().build()
    }
}
