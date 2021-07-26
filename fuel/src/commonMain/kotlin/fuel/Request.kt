package fuel

public class Request internal constructor(
    public val url: String,
    public val headers: Map<String, String>?,
    public val body: String?,
    public val method: String?
) {
    public open class Builder {
        private var url: String? = null
        private var headers = emptyMap<String, String>()
        private var body: String? = null
        private var method: String? = null

        /**
         * Set the url to load.
         */
         public fun url(url: String): Builder = apply {
            this.url = url
        }

        /**
         * Set the [MutableMap] for any network operations performed by this request.
         */
        public fun headers(headers: Map<String, String>): Builder {
            this.headers = headers
            return this
        }

        public fun body(body: String?): Builder {
            this.body = body
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
            checkNotNull(url) { "url == null" },
            headers,
            body,
            method
        )
    }
}