// Inspired by https://github.com/coil-kt/coil/blob/master/coil-base/src/main/java/coil/request/Request.kt

package fuel

import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.RequestBody

class Request internal constructor(
    val data: HttpUrl,
    val headers: Headers,
    val requestBody: RequestBody?,
    val method: String?
) {
    open class Builder {
        private var data: HttpUrl?
        private var headers: Headers.Builder?
        private var requestBody: RequestBody?
        private var method: String?

        constructor() {
            data = null
            headers = null
            requestBody = null
            method = null
        }

        constructor(request: Request) {
            data = request.data
            headers = request.headers.newBuilder()
            requestBody = request.requestBody
            method = request.method
        }

        /**
         * Set the data to load.
         */
        fun data(data: HttpUrl?) = apply {
            this.data = data
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
            this.headers = (this.headers ?: Headers.Builder()).add(name, value)
            return this
        }

        /**
         * Set a header for any network operations performed by this request.
         *
         * @see Headers.Builder.set
         */
        fun setHeader(name: String, value: String): Builder {
            this.headers = (this.headers ?: Headers.Builder()).set(name, value)
            return this
        }

        /**
         * Remove all network headers with the key [name].
         */
        fun removeHeader(name: String): Builder {
            this.headers = this.headers?.removeAll(name)
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
            headers?.build().orEmpty(),
            requestBody,
            method
        )
    }
}
