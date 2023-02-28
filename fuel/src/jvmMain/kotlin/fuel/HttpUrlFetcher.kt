// Inspired By https://github.com/coil-kt/coil/blob/master/coil-base/src/main/java/coil/fetch/HttpFetcher.kt

package fuel

import okhttp3.Call
import okhttp3.Request

internal class HttpUrlFetcher(private val callFactory: Call.Factory) {
    fun fetch(request: fuel.Request, builder: Request.Builder): Call {
        val urlString = request.parameters?.let {
            request.url.fillURLWithParameters(it)
        } ?: request.url
        builder.url(urlString)
        return callFactory.newCall(builder.build())
    }
}
