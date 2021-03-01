// Inspired by https://github.com/coil-kt/coil/blob/master/coil-base/src/main/java/coil/RealImageLoader.kt

package fuel

import okhttp3.Call
import okhttp3.Request.Builder
import okhttp3.Response

private enum class Method {
    GET,
    HEAD,
    POST,
    DELETE,
    PUT,
    PATCH
}

internal class RealHttpLoader(callFactory: Call.Factory) : HttpLoader {

    private val fetcher by lazy { HttpUrlFetcher(callFactory) }

    //region call implementation
    override fun get(request: Request): Call {
        return fetcher.fetch(request.data, createRequestBuilder(request, Method.GET))
    }

    override fun post(request: Request): Call {
        requireNotNull(request.requestBody, { "RequestBody for method POST should not be null" })
        return fetcher.fetch(request.data, createRequestBuilder(request, Method.POST))
    }

    override fun put(request: Request): Call {
        requireNotNull(request.requestBody, { "RequestBody for method PUT should not be null" })
        return fetcher.fetch(request.data, createRequestBuilder(request, Method.PUT))
    }

    override fun patch(request: Request): Call {
        requireNotNull(request.requestBody, { "RequestBody for method PATCH should not be null" })
        return fetcher.fetch(request.data, createRequestBuilder(request, Method.PATCH))
    }

    override fun delete(request: Request): Call {
        return fetcher.fetch(request.data, createRequestBuilder(request, Method.DELETE))
    }

    override fun head(request: Request): Call {
        return fetcher.fetch(request.data, createRequestBuilder(request, Method.HEAD))
    }

    override fun method(request: Request): Call {
        val method = requireNotNull(request.method, { "method should be not null" })
        val requestBuilder = Builder().headers(request.headers).method(method, request.requestBody)
        return fetcher.fetch(request.data, requestBuilder)
    }
    //endregion
}

internal class RealSuspendHttpLoader(callFactory: Call.Factory) : SuspendHttpLoader {

    private val fetcher by lazy { HttpUrlFetcher(callFactory) }

    //region suspend implementation
    override suspend fun get(request: Request): Response {
        return fetcher.fetch(request.data, createRequestBuilder(request, Method.GET)).await().validate()
    }

    override suspend fun post(request: Request): Response {
        requireNotNull(request.requestBody, { "RequestBody for method POST should not be null" })
        return fetcher.fetch(request.data, createRequestBuilder(request, Method.POST)).await().validate()
    }

    override suspend fun put(request: Request): Response {
        requireNotNull(request.requestBody, { "RequestBody for method PUT should not be null" })
        return fetcher.fetch(request.data, createRequestBuilder(request, Method.PUT)).await().validate()
    }

    override suspend fun patch(request: Request): Response {
        requireNotNull(request.requestBody, { "RequestBody for method PATCH should not be null" })
        return fetcher.fetch(request.data, createRequestBuilder(request, Method.PATCH)).await().validate()
    }

    override suspend fun delete(request: Request): Response {
        return fetcher.fetch(request.data, createRequestBuilder(request, Method.DELETE)).await().validate()
    }

    override suspend fun head(request: Request): Response {
        return fetcher.fetch(request.data, createRequestBuilder(request, Method.HEAD)).await().validate()
    }

    override suspend fun method(request: Request): Response {
        val method = requireNotNull(request.method, { "method should be not null" })
        val requestBuilder = Builder().headers(request.headers).method(method, request.requestBody)
        return fetcher.fetch(request.data, requestBuilder).await().validate()
    }
    //endregion
}

private fun createRequestBuilder(request: Request, method: Method): Builder {
    return Builder().headers(request.headers).apply {
        if (method == Method.GET || method == Method.HEAD) {
            method(method.name, null)
        } else {
            method(method.name, request.requestBody)
        }
    }
}

internal fun Response.validate(): Response {
    if (!isSuccessful) {
        throw HttpException(this)
    }
    return this
}
