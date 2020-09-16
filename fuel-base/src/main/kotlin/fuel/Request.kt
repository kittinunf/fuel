// Inspired by https://github.com/coil-kt/coil/blob/master/coil-base/src/main/java/coil/request/Request.kt

package fuel

import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.RequestBody

public class Request internal constructor(
    public val data: HttpUrl,
    public val headers: Headers,
    public val requestBody: RequestBody?,
    public val method: String?
) {
    public open class Builder {
        private var data: HttpUrl? = null
        private var headers = Headers.Builder()
        private var requestBody: RequestBody? = null
        private var method: String? = null

        /**
         * Set the data to load.
         */
        public fun data(data: HttpUrl): Builder = apply {
            this.data = data
        }

        public fun data(data: String): Builder = apply {
            // Silently replace web socket URLs with HTTP URLs.
            val finalUrl: String = when {
                data.startsWith("ws:", ignoreCase = true) -> {
                    "http:${data.substring(3)}"
                }
                data.startsWith("wss:", ignoreCase = true) -> {
                    "https:${data.substring(4)}"
                }
                else -> data
            }
            this.data = finalUrl.toHttpUrl()
        }

        /**
         * Set the [Headers] for any network operations performed by this request.
         */
        public fun headers(headers: Headers): Builder {
            this.headers = headers.newBuilder()
            return this
        }

        /**
         * Add a header for any network operations performed by this request.
         *
         * @see Headers.Builder.add
         */
        public fun addHeader(name: String, value: String): Builder {
            this.headers = this.headers.add(name, value)
            return this
        }

        /**
         * Set a header for any network operations performed by this request.
         *
         * @see Headers.Builder.set
         */
        public fun setHeader(name: String, value: String): Builder {
            this.headers = this.headers.set(name, value)
            return this
        }

        /**
         * Remove all network headers with the key [name].
         */
        public fun removeHeader(name: String): Builder {
            this.headers = this.headers.removeAll(name)
            return this
        }

        public fun requestBody(requestBody: RequestBody?): Builder {
            this.requestBody = requestBody
            return this
        }

        public fun method(method: String?): Builder {
            this.method = method
            return this
        }

        /**
         * Create a new [Request] instance.
         */
        public fun build(): Request = Request(
            checkNotNull(data) { "data == null" },
            headers.build(),
            requestBody,
            method
        )
    }
}
