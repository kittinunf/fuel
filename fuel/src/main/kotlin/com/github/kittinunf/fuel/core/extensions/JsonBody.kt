package com.github.kittinunf.fuel.core.extensions

import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Request
import java.nio.charset.Charset

/**
 * Set the body to a JSON string and automatically set the json content type
 */
fun Request.jsonBody(body: String, charset: Charset = Charsets.UTF_8): Request {
    this[Headers.CONTENT_TYPE] = "application/json"
    return body(body, charset)
}