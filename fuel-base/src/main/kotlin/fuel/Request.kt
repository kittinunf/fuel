// Inspired by https://github.com/coil-kt/coil/blob/master/coil-base/src/main/java/coil/request/Request.kt

package fuel

import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.RequestBody

class Request internal constructor(
    val data: HttpUrl,
    val headers: Headers,
    val requestBody: RequestBody?,
    val method: String?
) {
    open class Builder {
        private var data: HttpUrl? = null
        private var headers = Headers.Builder()
        private var requestBody: RequestBody? = null
        private var method: String? = null

        /**
         * Set the data to load.
         */
        fun data(data: HttpUrl) = apply {
            this.data = data
        }

        fun data(data: String) = apply {
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
        fun headers(headers: Headers): Builder {
            this.headers = headers.newBuilder()
            return this
        }

        /**
         * Add a header for any network operations performed by this request.
         *
         * @see Headers.Builder.add
         */
        fun addHeader(name: String, value: String): Builder {
            this.headers = this.headers.add(name, value)
            return this
        }

        /**
         * Set a header for any network operations performed by this request.
         *
         * @see Headers.Builder.set
         */
        fun setHeader(name: String, value: String): Builder {
            this.headers = this.headers.set(name, value)
            return this
        }

        /**
         * Remove all network headers with the key [name].
         */
        fun removeHeader(name: String): Builder {
            this.headers = this.headers.removeAll(name)
            return this
        }

        fun requestBody(requestBody: RequestBody?): Builder {
            this.requestBody = requestBody
            return this
        }

        fun method(method: String?): Builder {
            this.method = method
            return this
        }

        /**
         * Create a new [Request] instance.
         */
        fun build() = Request(
            checkNotNull(data) { "data == null" },
            headers.build(),
            requestBody,
            method
        )
    }
}
