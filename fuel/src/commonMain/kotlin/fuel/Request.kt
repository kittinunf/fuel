package fuel

public typealias Parameters = List<Pair<String, String>>

public class Request internal constructor(
    public val url: String,
    public val parameters: Parameters?,
    public val headers: Map<String, String>?,
    public val body: String?,
    public val method: String?
) {
    public open class Builder {
        private var url: String? = null
        private var headers = emptyMap<String, String>()
        private var body: String? = null
        private var method: String? = null
        private var parameters: Parameters? = null

        /**
         * Set the url to load
         */
        public fun url(url: String): Builder = apply {
            this.url = url
        }

        public fun parameters(parameters: Parameters?): Builder = apply {
            this.parameters = parameters
        }

        /**
         * Set the [MutableMap] for any network operations performed by this request.
         */
        public fun headers(headers: Map<String, String>): Builder = apply {
            this.headers = headers
        }

        public fun body(body: String?): Builder = apply {
            this.body = body
        }

        public fun method(method: String?): Builder = apply {
            this.method = method
        }

        /**
         * Create a new [Request] instance.
         */
        public fun build(): Request = Request(
            checkNotNull(url) { "url == null" },
            parameters,
            headers,
            body,
            method
        )
    }
}
