package fuel

import platform.Foundation.NSURLSessionConfiguration

public actual class HttpLoader(sessionConfiguration: NSURLSessionConfiguration)  {
    private val fetcher by lazy { HttpUrlFetcher(sessionConfiguration) }

    public actual suspend fun get(request: Request): HttpResponse =
        fetcher.fetch("GET", request)

    public actual suspend fun post(request: Request): HttpResponse {
        requireNotNull(request.body) { "body for method POST should not be null" }
        return fetcher.fetch("POST", request)
    }

    public actual suspend fun put(request: Request): HttpResponse {
        requireNotNull(request.body) { "body for method PUT should not be null" }
        return fetcher.fetch("PUT", request)
    }

    public actual suspend fun patch(request: Request): HttpResponse {
        requireNotNull(request.body) { "body for method PATCH should not be null" }
        return fetcher.fetch("PATCH", request)
    }

    public actual suspend fun delete(request: Request): HttpResponse = fetcher.fetch("DELETE", request)

    public actual suspend fun head(request: Request): HttpResponse = fetcher.fetch("HEAD", request)

    public actual suspend fun method(request: Request): HttpResponse {
        val method = requireNotNull(request.method) { "method should be not null" }
        return fetcher.fetch(method, request)
    }

    public companion object {
        public operator fun invoke(): HttpLoader = FuelBuilder().build()
    }
}
