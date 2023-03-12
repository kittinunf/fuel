package fuel

public actual class HttpLoader {
    private val fetcher by lazy { HttpUrlFetcher() }

    public actual suspend fun get(request: Request): HttpResponse {
        return fetcher.fetch(request, "GET")
    }

    public actual suspend fun post(request: Request): HttpResponse {
        requireNotNull(request.body) { "body for method POST should not be null" }
        return fetcher.fetch(request, "POST", request.body)
    }

    public actual suspend fun put(request: Request): HttpResponse {
        requireNotNull(request.body) { "body for method POST should not be null" }
        return fetcher.fetch(request, "PUT", request.body)
    }

    public actual suspend fun patch(request: Request): HttpResponse {
        requireNotNull(request.body) { "body for method POST should not be null" }
        return fetcher.fetch(request, "PATCH", request.body)
    }

    public actual suspend fun delete(request: Request): HttpResponse {
        return fetcher.fetch(request, "DELETE")
    }

    public actual suspend fun head(request: Request): HttpResponse {
        return fetcher.fetch(request, "HEAD")
    }

    public actual suspend fun method(request: Request): HttpResponse {
        val method = requireNotNull(request.method) { "method should be not null" }
        return fetcher.fetch(request, method, request.body)
    }
}
