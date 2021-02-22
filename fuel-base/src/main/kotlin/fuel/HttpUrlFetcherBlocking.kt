package fuel

import okhttp3.Call
import okhttp3.HttpUrl
import okhttp3.Request
import okhttp3.Response

public class HttpUrlFetcherBlocking(private val callFactory: Call.Factory) {

    public fun fetchBlocking(httpUrl: HttpUrl, builder: Request.Builder): Response {
        builder.url(httpUrl)
        val response = callFactory.newCall(builder.build()).execute()
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
        return response
    }
}
