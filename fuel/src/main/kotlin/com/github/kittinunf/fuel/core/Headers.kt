package com.github.kittinunf.fuel.core

data class HeaderName(val name: String) {
    private val normalized = name.toUpperCase()
    override fun hashCode(): Int {
        return normalized.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return (other is HeaderName) && other.normalized === normalized
    }
}

typealias HeaderValues = List<String>

class Headers: MutableMap<String, HeaderValues> {

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

    fun appendAll(key: String, value: List<Any>) {
        val current = contents.getOrPut(HeaderName(key)) { emptyList() }
        put(key, current.plus(value.map { it.toString() }))
    }

    fun append(key: String, value: Any) {
        val current = contents.getOrPut(HeaderName(key)) { emptyList() }
        put(key, current.plus(value.toString()))
    }

    fun replace(key: String, value: String) {
        put(key, listOf(value))
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
    override fun get(key: String): HeaderValues? {
        return contents[HeaderName(key)]
    }

    operator fun set(key: String, value: String) {
        replace(key, value)
    }

    operator fun set(key: String, values: HeaderValues) {
        put(key, values)
    }

    /**
     * Returns `true` if the map is empty (contains no elements), `false` otherwise.
     */
    override fun isEmpty(): Boolean {
        return contents.isEmpty()
    }

    companion object {
        const val AUTHORIZATION = "Authorization"
        const val CONTENT_TYPE = "Content-Type"
    }
}