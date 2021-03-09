// Inspired by https://github.com/coil-kt/coil/blob/master/coil-base/src/main/java/coil/RealImageLoader.kt

package fuel

import okhttp3.Call
import okhttp3.Request.Builder
import okhttp3.Response
import okhttp3.internal.http.HttpMethod

internal class RealHttpLoader(callFactory: Call.Factory) : HttpLoader {

    private val fetcher by lazy { HttpUrlFetcher(callFactory) }

    //region call implementation
    override fun get(request: Request): Call {
        return fetcher.fetch(request.data, createRequestBuilder(request, "GET"))
    }

    override fun post(request: Request): Call {
        requireNotNull(request.requestBody, { "RequestBody for method POST should not be null" })
        return fetcher.fetch(request.data, createRequestBuilder(request, "POST"))
    }

    override fun put(request: Request): Call {
        requireNotNull(request.requestBody, { "RequestBody for method PUT should not be null" })
        return fetcher.fetch(request.data, createRequestBuilder(request, "PUT"))
    }

    override fun patch(request: Request): Call {
        requireNotNull(request.requestBody, { "RequestBody for method PATCH should not be null" })
        return fetcher.fetch(request.data, createRequestBuilder(request, "PATCH"))
    }

    override fun delete(request: Request): Call {
        return fetcher.fetch(request.data, createRequestBuilder(request, "DELETE"))
    }

    override fun head(request: Request): Call {
        return fetcher.fetch(request.data, createRequestBuilder(request, "HEAD"))
    }

    override fun method(request: Request): Call {
        val method = requireNotNull(request.method, { "method should be not null" })
        return fetcher.fetch(request.data, createRequestBuilder(request, method))
    }
    //endregion
}

private fun createRequestBuilder(request: Request, method: String): Builder =
    Builder().headers(request.headers).apply {
        if (HttpMethod.permitsRequestBody(method)) {
            method(method, request.requestBody)
        } else {
            method(method, null)
        }
    }

internal fun Response.validate(): Response {
    if (!isSuccessful) {
        throw HttpException(this)
    }
    return this
}
