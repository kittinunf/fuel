package fuel

import platform.Foundation.NSURLSessionConfiguration

public class AppleHttpLoader(sessionConfiguration: NSURLSessionConfiguration) : HttpLoader {
    private val fetcher by lazy { HttpUrlFetcher(sessionConfiguration) }

    public override suspend fun get(request: Request): HttpResponse =
        fetcher.fetch("GET", request)

    public override suspend fun post(request: Request): HttpResponse {
        requireNotNull(request.body) { "body for method POST should not be null" }
        return fetcher.fetch("POST", request)
    }

    public override suspend fun put(request: Request): HttpResponse {
        requireNotNull(request.body) { "body for method PUT should not be null" }
        return fetcher.fetch("PUT", request)
    }

    public override suspend fun patch(request: Request): HttpResponse {
        requireNotNull(request.body) { "body for method PATCH should not be null" }
        return fetcher.fetch("PATCH", request)
    }

    public override suspend fun delete(request: Request): HttpResponse = fetcher.fetch("DELETE", request)

    public override suspend fun head(request: Request): HttpResponse = fetcher.fetch("HEAD", request)

    public override suspend fun method(request: Request): HttpResponse {
        val method = requireNotNull(request.method) { "method should be not null" }
        return fetcher.fetch(method, request)
    }

    public companion object {
        public operator fun invoke(): HttpLoader = FuelBuilder().build()
    }
}
