package fuel

public typealias Parameters = List<Pair<String, String>>

public class Request(
    public val url: String,
    public val parameters: Parameters?,
    public val headers: Map<String, String>?,
    public val body: String?,
    public val method: String?
) {
    private constructor(builder: Builder) : this(
        checkNotNull(builder.url) { "url == null" },
        builder.parameters,
        builder.headers,
        builder.body,
        builder.method
    )

    public class Builder {
        public var url: String? = null
        public var headers: Map<String, String> = emptyMap()
        public var body: String? = null
        public var method: String? = null
        public var parameters: Parameters? = null

        /**
         * Create a new [Request] instance.
         */
        public fun build(): Request = Request(this)
    }
}
