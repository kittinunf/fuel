// Inspired By https://github.com/coil-kt/coil/blob/master/coil-base/src/main/java/coil/fetch/HttpFetcher.kt

package fuel

import okhttp3.Call
import okhttp3.HttpUrl
import okhttp3.Request

internal class HttpUrlFetcher(private val callFactory: Call.Factory) {

    fun fetch(httpUrl: HttpUrl, builder: Request.Builder): Call {
        builder.url(httpUrl)
        return callFactory.newCall(builder.build())
    }
}
