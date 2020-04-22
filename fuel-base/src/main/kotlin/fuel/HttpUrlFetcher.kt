// Inspired By https://github.com/coil-kt/coil/blob/master/coil-base/src/main/java/coil/fetch/HttpFetcher.kt

package fuel

import okhttp3.Call
import okhttp3.HttpUrl
import okhttp3.Request.Builder
import okhttp3.Response

internal class HttpUrlFetcher(private val callFactory: Call.Factory) {
    suspend fun fetch(httpUrl: HttpUrl, builder: Builder): Response {
        builder.url(httpUrl)
        val response = callFactory.newCall(builder.build()).await()
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
        return response
    }
}
