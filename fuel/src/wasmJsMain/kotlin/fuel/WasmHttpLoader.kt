package fuel

public class WasmHttpLoader : HttpLoader {
    private val fetcher by lazy { HttpUrlFetcher() }

    public override suspend fun get(request: Request.Builder.() -> Unit): HttpResponse {
        val requestBuilder = Request.Builder().apply(request).build()
        return fetcher.fetch(requestBuilder, "GET")
    }

    public override suspend fun post(request: Request.Builder.() -> Unit): HttpResponse {
        val requestBuilder = Request.Builder().apply(request).build()
        requireNotNull(requestBuilder.body) { "body for method POST should not be null" }
        return fetcher.fetch(requestBuilder, "POST", requestBuilder.body)
    }

    public override suspend fun put(request: Request.Builder.() -> Unit): HttpResponse {
        val requestBuilder = Request.Builder().apply(request).build()
        requireNotNull(requestBuilder.body) { "body for method POST should not be null" }
        return fetcher.fetch(requestBuilder, "PUT", requestBuilder.body)
    }

    public override suspend fun patch(request: Request.Builder.() -> Unit): HttpResponse {
        val requestBuilder = Request.Builder().apply(request).build()
        requireNotNull(requestBuilder.body) { "body for method POST should not be null" }
        return fetcher.fetch(requestBuilder, "PATCH", requestBuilder.body)
    }

    public override suspend fun delete(request: Request.Builder.() -> Unit): HttpResponse {
        val requestBuilder = Request.Builder().apply(request).build()
        return fetcher.fetch(requestBuilder, "DELETE")
    }

    public override suspend fun head(request: Request.Builder.() -> Unit): HttpResponse {
        val requestBuilder = Request.Builder().apply(request).build()
        return fetcher.fetch(requestBuilder, "HEAD")
    }

    public override suspend fun method(request: Request.Builder.() -> Unit): HttpResponse {
        val requestBuilder = Request.Builder().apply(request).build()
        val method = requireNotNull(requestBuilder.method) { "method should be not null" }
        return fetcher.fetch(requestBuilder, method, requestBuilder.body)
    }
}
