// Inspired By https://github.com/coil-kt/coil/blob/master/coil-base/src/main/java/coil/fetch/HttpFetcher.kt

package fuel

import okhttp3.Call
import okhttp3.Request

internal class HttpUrlFetcher(
    private val callFactory: Lazy<Call.Factory>,
) {
    fun fetch(
        request: fuel.Request,
        builder: Request.Builder,
    ): Call {
        if (request.parameters != null) {
            builder.url(request.url.fillURLWithParameters(request.parameters))
        } else {
            builder.url(request.url)
        }
        return callFactory.value.newCall(builder.build())
    }
}
