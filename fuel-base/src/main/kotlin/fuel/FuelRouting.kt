package fuel

import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.RequestBody

interface FuelRouting : RequestConvertible {
    /**
     * Base path handler for the remote call.
     */
    val basePath: String
    /**
     * Method handler for the remote requests.
     */
    val method: String
    /**
     * Path handler for the request.
     */
    val path: String
    /**
     * Headers for remote call.
     */
    val headers: Headers.Builder?
    /**
     * Body to handle other type of request (e.g. application/json )
     */
    val body: RequestBody?

    override val request: Request
        get() = Request(
                "$basePath/$path".toHttpUrl(),
                headers?.build().orEmpty(),
                body,
                method
        )
}
