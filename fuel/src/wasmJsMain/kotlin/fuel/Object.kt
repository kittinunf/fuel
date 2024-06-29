package fuel

/**
 * Return empty JS Object
 */
public fun obj(): JsAny = js("{}")

/**
 * Helper function for creating JavaScript objects with given type.
 */
public inline fun <T : JsAny> obj(init: T.() -> Unit): T {
    return (obj().unsafeCast<T>()).apply(init)
}

/**
 * Operator to set property on JS Object
 */
public operator fun JsAny.set(
    key: String,
    value: JsAny,
) {
    this[key] = value
}

public fun getKeys(headers: org.w3c.fetch.Headers): JsArray<JsString> = js("Array.from(headers.keys())")
