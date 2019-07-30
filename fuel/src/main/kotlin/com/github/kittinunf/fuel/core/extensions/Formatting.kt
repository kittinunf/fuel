package com.github.kittinunf.fuel.core.extensions

import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request

/**
 * Returns a representation that can be used over the HTTP protocol
 *
 * @see toString
 * @see cUrlString
 *
 * @return [String] the string representation
 */
fun Request.httpString(): String = buildString {
    // url
    val params = parameters.joinToString(separator = "&", prefix = "?") { "${it.first}=${it.second}" }
    appendln("${method.value} $url$params")
    appendln()
    // headers

    val appendHeaderWithValue = { key: String, value: String -> appendln("$key : $value") }
    headers.transformIterate(appendHeaderWithValue)

    // body
    body(body.asRepeatable())
    appendln()
    appendln(String(body.toByteArray()))
}

/**
 * Returns a representation that can be used with cURL
 *
 * @see com.github.kittinunf.fuel.core.Request.toString
 * @see httpString
 *
 * @return [String] the string representation
 */
fun Request.cUrlString(): String = buildString {
    append("curl -i")

    // method
    if (method != Method.GET) {
        append(" -X $method")
    }

    // body
    body(body.asRepeatable())
    val escapedBody = String(body.toByteArray()).replace("\"", "\\\"")
    if (escapedBody.isNotEmpty()) {
        append(" -d \"$escapedBody\"")
    }

    // headers
    val appendHeaderWithValue = { key: String, value: String -> append(" -H \"$key:$value\"") }
    headers.transformIterate(appendHeaderWithValue)

    // url
    append(" $url")
}
