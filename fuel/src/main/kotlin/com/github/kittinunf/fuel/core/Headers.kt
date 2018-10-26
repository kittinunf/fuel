package com.github.kittinunf.fuel.core

data class HeaderName(val name: String) {
    private val normalized = name.toUpperCase()
    override fun hashCode(): Int {
        return normalized.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return (other is HeaderName) && other.normalized == normalized
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
     * @return the previous value associated with the key, or `null` if the key was not present in the map.
     */
    override fun put(key: String, value: HeaderValues): HeaderValues? {
        return contents.put(HeaderName(key), value)
    }

    fun append(key: String, value: Collection<*>): Headers {
        val current = contents.getOrPut(HeaderName(key)) { emptyList() }
        put(key, current.plus(value.map { it.toString() }))
        return this
    }

    fun append(key: String, value: Any): Headers {
        // Some headers can not be appended per the RFC
        if (Headers.isSingleValue(HeaderName(key))) {
            set(key, value.toString())
            return this
        }

        put(key, this[key].plus(value.toString()))
        return this
    }

    fun replace(key: String, value: String): Headers {
        put(key, listOf(value))
        return this
    }

    /**
     * Updates this map with key/value pairs from the specified map [from].
     */
    override fun putAll(from: Map<out String, HeaderValues>) {
        // No need to map the `from` input to a map with header name
        from.forEach { put(it.key, it.value) }
    }

    /**
     * Removes the specified key and its corresponding value from this map.
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
     */
    override fun get(key: String): HeaderValues {
        return contents[HeaderName(key)].orEmpty()
    }

    operator fun set(key: String, value: String) = replace(key, value)

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

    fun transformIterate(set: (key: String, value: String) -> Any, add: (key: String, value: String) -> Any = set) {
        for ((key, values) in this) {
            val header = HeaderName(key)
            when (Headers.isCollapsible(header)) {
                true ->
                    // NOTE: HTTP requires all request properties which can
                    //     * legally have multiple instances with the same key
                    //     * to use a comma-separated list syntax which enables multiple
                    //     * properties to be appended into a single property.
                    set(key, Headers.collapse(header, values))
                false -> when (Headers.isSingleValue(header)) {
                    true -> values.lastOrNull()?.let { set(key, it) }
                    false -> values.forEach { add(key, it) }
                }
            }
        }
    }

    override fun toString(): String {
        return contents.toString()
    }

    companion object {
        fun isCollapsible(key: HeaderName): Boolean {
            return when (key) {
                // These headers, per RFC, SHOULD NOT be collapsed into a single value
                HeaderName(SET_COOKIE) -> false
                else -> true
            }
        }

        fun isSingleValue(key: HeaderName): Boolean {
            return when (key) {
                // These headers, per RFC, SHOULD only appear once.
                HeaderName(AGE) -> true
                HeaderName(CONTENT_ENCODING) -> true
                HeaderName(CONTENT_LENGTH) -> true
                HeaderName(CONTENT_LOCATION) -> true
                HeaderName(CONTENT_TYPE) -> true
                HeaderName(EXPECT) -> true
                HeaderName(EXPIRES) -> true
                HeaderName(LOCATION) -> true
                else -> false
            }
        }

        fun collapse(header: HeaderName, values: HeaderValues): String {
            return when (header) {
                HeaderName(COOKIE) -> values.joinToString("; ")
                else -> values.joinToString(", ")
            }
        }

        fun from(mapOf: Map<String, Any>): Headers {
            return mapOf.entries.fold(Headers()) { result, entry ->
                // The issue here is that connection.headerFields is typed as Map<String, List<String>> but in fact is
                //   a Map<String?, List<String>> and thus needs this cast here or it will break as an
                //   java.lang.IllegalArgumentException: Parameter specified as non-null is null
                //
                val key = (entry.key as String?).orEmpty().ifBlank { null } ?: return@fold result
                val value = entry.value

                when (value) {
                    is Collection<*> -> {
                        val values = value.ifEmpty { null } ?: return@fold result
                        result.set(key, values.map { v -> v.toString() })
                    }
                    else -> result.set(key, value.toString())
                }
            }
        }

        fun from(vararg pairs: Pair<String, Any>): Headers {
            return from(pairs.toMap())
        }

        const val ACCEPT_ENCODING = "Accept-Encoding"
        const val ACCEPT_TRANSFER_ENCODING = "TE"
        const val AGE = "Age"
        const val AUTHORIZATION = "Authorization"
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
    }
}