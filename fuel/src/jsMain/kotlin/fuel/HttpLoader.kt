package fuel

public actual class HttpLoader {
    private val fetcher by lazy { HttpUrlFetcher() }

    public actual suspend fun get(request: Request): HttpResponse = fetcher.fetch(request.url, "GET")

    public actual suspend fun post(request: Request): HttpResponse {
        requireNotNull(request.body) { "body for method POST should not be null" }
        return fetcher.fetch(request.url, "POST", request.body)
    }

    public actual suspend fun put(request: Request): HttpResponse {
        requireNotNull(request.body) { "body for method POST should not be null" }
        return fetcher.fetch(request.url, "PUT", request.body)
    }

    public actual suspend fun patch(request: Request): HttpResponse {
        requireNotNull(request.body) { "body for method POST should not be null" }
        return fetcher.fetch(request.url, "PATCH", request.body)
    }

    public actual suspend fun delete(request: Request): HttpResponse = fetcher.fetch(request.url, "DELETE")

    public actual suspend fun head(request: Request): HttpResponse = fetcher.fetch(request.url, "HEAD")

    public actual suspend fun method(request: Request): HttpResponse {
        val method = requireNotNull(request.method) { "method should be not null" }
        return fetcher.fetch(request.url, method, request.body)
    }
}