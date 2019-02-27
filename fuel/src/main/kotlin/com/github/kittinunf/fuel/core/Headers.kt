package com.github.kittinunf.fuel.core

/**
 * Special case-insensitive string wrapper that can be used as key to lookup headers.
 *
 * @see Headers
 * @param name [String] the header name
 */
data class HeaderName(val name: String) {
    private val normalized = name.toUpperCase()

    override fun hashCode(): Int {
        return normalized.hashCode()
    }

    /**
     * Equals any string or header name that is equals without regarding case
     */
    override fun equals(other: Any?): Boolean {
        return when (other) {
            is HeaderName -> other.normalized == normalized
            is String -> HeaderName(other).normalized == normalized
            else -> false
        }
    }

    override fun toString(): String {
        return name
    }
}

typealias HeaderValues = Collection<String>

class Headers : MutableMap<String, HeaderValues> {

    private var contents: HashMap<HeaderName, HeaderValues> = HashMap()

    /**
     * Returns a [MutableSet] of all key/value pairs in this map.
     */
    override val entries: MutableSet<MutableMap.MutableEntry<String, HeaderValues>>
        get() = contents.mapKeys { it.key.name }.toMutableMap().entries

    /**
     * Returns a [MutableSet] of all keys in this map.
     */
    override val keys: MutableSet<String>
        get() = HashSet(contents.keys.map { it.name }).toMutableSet()

    /**
     * Returns a [MutableCollection] of all values in this map. Note that this collection may contain duplicate values.
     */
    override val values: MutableCollection<HeaderValues>
        get() = contents.values

    /**
     * Removes all elements from this map.
     */
    override fun clear() {
        contents.clear()
    }

    /**
     * Associates the specified [value] with the specified [key] in the map.
     *
     * @param key [String] the header name
     * @param value [HeaderValues] the new values
     * @return [HeaderValues?] the previous value at [key], or `null` if the [key] was not present in the map.
     */
    override fun put(key: String, value: HeaderValues): HeaderValues? {
        return contents.put(HeaderName(key), value)
    }

    /**
     * Appends the entire [values] to the current values at [header]
     *
     * @param header [String] the header to append to
     * @param values [Collection] the values to append, coerced with [Any.toString]
     */
    fun append(header: String, values: Collection<*>): Headers {
        put(header, this[header].plus(values.map { it.toString() }))
        return this
    }

    /**
     * Appends a single value [value] to the current values at [header]
     *
     * @param header [String] the header to append to
     * @param value [Any] the value to append, coerced with [Any.toString]
     */
    fun append(header: String, value: Any): Headers {
        return when (Headers.isSingleValue(header)) {
            true -> set(header, value.toString())
            false -> set(header, this[header].plus(value.toString()))
        }
    }

    /**
     * Updates this map with key/value pairs from the specified map [from].
     *
     */
    override fun putAll(from: Map<out String, HeaderValues>) {
        // The call to [Headers.from] actually filters out invalid entries (duplicate headers which are isSingle), and
        // makes sure that multiple values for a single Header are all added (e.g. "foo" to "a", "foo" to "b").
        Headers.from(from).forEach {
            put(it.key, it.value)
        }
    }

    /**
     * Removes the specified [key] and its corresponding value from this map.
     *
     * @return the previous value associated with the key, or `null` if the key was not present in the map.
     */
    override fun remove(key: String): HeaderValues? {
        return contents.remove(HeaderName(key))
    }

    /**
     * Returns the number of key/value pairs in the map.
     */
    override val size: Int
        get() = contents.size

    /**
     * Returns `true` if the map contains the specified [key].
     */
    override fun containsKey(key: String): Boolean {
        return contents.containsKey(HeaderName(key))
    }

    /**
     * Returns `true` if the map maps one or more keys to the specified [value].
     */
    override fun containsValue(value: HeaderValues): Boolean {
        return contents.containsValue(value)
    }

    /**
     * Returns the value corresponding to the given [key], or `null` if such a key is not present in the map.
     *
     * @param key [String] the header name
     * @return [HeaderValues] the values at [key] or an empty list
     */
    override fun get(key: String): HeaderValues {
        val header = HeaderName(key)
        return contents[header].orEmpty().let {
            when (Headers.isSingleValue(header)) {
                true -> listOfNotNull(it.lastOrNull())
                false -> it
            }
        }
    }

    /**
     * @see put
     */
    operator fun set(key: String, value: String): Headers {
        put(key, listOf(value))
        return this
    }

    /**
     * @see put
     */
    operator fun set(key: String, values: HeaderValues): Headers {
        put(key, values)
        return this
    }

    /**
     * Returns `true` if the map is empty (contains no elements), `false` otherwise.
     */
    override fun isEmpty(): Boolean {
        return contents.isEmpty()
    }

    /**
     * Iterates all the keys and values, going through the RFC defined rules for each one and when applicable, calling
     * one of two callbacks [set] or [add].
     *
     * @see isCollapsible
     * @see collapse
     * @see isSingleValue
     */
    fun transformIterate(set: (key: String, value: String) -> Any?, add: (key: String, value: String) -> Any? = set) {
        for ((key, values) in this) {
            val header = HeaderName(key)
            when (Headers.isCollapsible(header)) {
                true ->
                    // From the [HttpURLConnection.setRequestProperty](https://docs.oracle.com/javase/7/docs/api/java/net/URLConnection.html#setRequestProperty(java.lang.String,%20java.lang.String))
                    //
                    //   NOTE: HTTP requires all request properties which can
                    //         legally have multiple instances with the same key
                    //         to use a comma-separated list syntax which enables multiple
                    //         properties to be appended into a single property.
                    //
                    set(key, Headers.collapse(header, values))
                false -> when (Headers.isSingleValue(header)) {
                    true -> values.lastOrNull()?.let { set(key, it) }
                    false -> values.forEach { add(key, it) }
                }
            }
        }
    }

    override fun toString() = contents.toString()

    companion object {

        /**
         * Determines if a multiple values may be collapsed into an ordered list.
         *
         * @see isSingleValue
         * @see collapse
         *
         * RFC 7230 links to RFC 6265 stating that:
         *
         *  Note: In practice, the "Set-Cookie" header field (RFC6265) often
         *  appears multiple times in a response message and does not use the
         *  list syntax, violating the above requirements on multiple header
         *  fields with the same name.  Since it cannot be combined into a
         *  single field-value, recipients ought to handle "Set-Cookie" as a
         *  special case while processing header fields.  (See Appendix A.2.3
         *  of Kri2001 for details.)
         *
         * @see [RFC 7230](https://tools.ietf.org/html/rfc7230#section-3.2.2))
         * @see [RFC 6265](https://tools.ietf.org/html/rfc6265#section-5.2)
         *
         * @param header [HeaderName] the header to check
         * @return [Boolean] true if can be collapsed into a single value
         */
        fun isCollapsible(header: HeaderName) =
            COLLAPSIBLE_HEADERS.getOrElse(header) { !isSingleValue(header) }

        /**
         * This function determines if a header is allowed to have multiple values.
         *
         * RFC 7230, 3.2.2 Field order states that:
         *
         *  A sender MUST NOT generate multiple header fields with the same field
         *  name in a message unless either the entire field value for that
         *  header field is defined as a comma-separated list [i.e., #(values)]
         *  or the header field is a well-known exception (as noted below).
         *
         * @see [RFC 7230](https://tools.ietf.org/html/rfc7230#section-3.2.2))
         *
         * @param header [HeaderName] the header to check
         * @return [Boolean] true if it is a single value, false otherwise
         */
        fun isSingleValue(header: HeaderName) =
            SINGLE_VALUE_HEADERS[header] ?: false

        /**
         * This functions collapses multiple HeaderValues into a single value.
         *
         * @see isSingleValue
         * @see isCollapsible
         *
         * RFC 7230, 3.2.2 Field order states that:
         *
         *  A recipient MAY combine multiple header fields with the same field
         *  name into one "field-name: field-value" pair, without changing the
         *  semantics of the message, by appending each subsequent field value to
         *  the combined field value in order, separated by a comma.  The order
         *  in which header fields with the same field name are received is
         *  therefore significant to the interpretation of the combined field
         *  value; a proxy MUST NOT change the order of these field values when
         *  forwarding a message.
         *
         * @param header [HeaderName] the header to check
         * @return [Boolean] true if it is a single value, false otherwise
         */
        fun collapse(header: HeaderName, values: HeaderValues) =
            values.joinToString(COLLAPSE_SEPARATOR[header] ?: ", ")

        fun isCollapsible(header: String) = isCollapsible(HeaderName(header))
        fun isSingleValue(header: String) = isSingleValue(HeaderName(header))
        fun collapse(header: String, values: HeaderValues) = collapse(HeaderName(header), values)

        /**
         * Creates a valid Headers from the [pairs] map, applying the rules defined by the RFCs.
         *
         * @return [Headers]
         */
        fun from(pairs: Collection<Pair<String, Any>>): Headers {
            return pairs.fold(Headers()) { result, entry ->
                // The issue here is that connection.headerFields is typed as Map<String, List<String>> but in fact is
                //   a Map<String?, List<String>> and thus needs this cast here or it will break as an
                //   java.lang.IllegalArgumentException: Parameter specified as non-null is null
                //
                val key = (entry.first as String?).orEmpty().ifBlank { null } ?: return@fold result
                val value = entry.second

                when (value) {
                    is Collection<*> -> {
                        val values = value.ifEmpty { null } ?: return@fold result
                        result.append(key, values.map { v -> v.toString() })
                    }
                    else -> result.append(key, value.toString())
                }
            }
        }

        fun from(source: Map<out String, Any>): Headers = from(source.entries.map { Pair(it.key, it.value) })
        fun from(vararg pairs: Pair<String, Any>) = from(pairs.toList())

        const val ACCEPT = "Accept"
        const val ACCEPT_ENCODING = "Accept-Encoding"
        const val ACCEPT_LANGUAGE = "Accept-Language"
        const val ACCEPT_TRANSFER_ENCODING = "TE"
        const val AGE = "Age"
        const val AUTHORIZATION = "Authorization"
        const val CACHE_CONTROL = "Cache-Control"
        const val CONTENT_DISPOSITION = "Content-Disposition"
        const val CONTENT_ENCODING = "Content-Encoding"
        const val CONTENT_LENGTH = "Content-Length"
        const val CONTENT_LOCATION = "Content-Location"
        const val CONTENT_TYPE = "Content-Type"
        const val COOKIE = "Cookie"
        const val EXPECT = "Expect"
        const val EXPIRES = "Expires"
        const val LOCATION = "Location"
        const val SET_COOKIE = "Set-Cookie"
        const val TRANSFER_ENCODING = "Transfer-Encoding"
        const val USER_AGENT = "User-Agent"

        /**
         * Below are lookup tables for various functions. The reason these are a map is that
         * - lookup is O(1) instead of O(n) for a list
         * - default value is `null`, making it easy to elvis to a non-null default
         * - no if statements necessary (instead of if(list.contains) { logic } else { logic})
         */

        private val COLLAPSIBLE_HEADERS = mapOf(
            HeaderName(SET_COOKIE) to false
        )

        private val SINGLE_VALUE_HEADERS = mapOf(
            HeaderName(AGE) to true,
            HeaderName(CONTENT_ENCODING) to true,
            HeaderName(CONTENT_LENGTH) to true,
            HeaderName(CONTENT_LOCATION) to true,
            HeaderName(CONTENT_TYPE) to true,
            HeaderName(EXPECT) to true,
            HeaderName(EXPIRES) to true,
            HeaderName(LOCATION) to true,
            HeaderName(USER_AGENT) to true
        )

        private val COLLAPSE_SEPARATOR = mapOf(
            HeaderName(COOKIE) to "; "
        )
    }
}
