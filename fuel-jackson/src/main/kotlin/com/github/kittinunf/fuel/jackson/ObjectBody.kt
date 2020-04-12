package com.github.kittinunf.fuel.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Request
import java.nio.charset.Charset

/**
 * Set the body to an Object to be serialized
 */
fun Request.objectBody(
    bodyObject: Any,
    charset: Charset = Charsets.UTF_8,
    mapper: ObjectMapper = defaultMapper
): Request {
    val bodyString = mapper.writeValueAsString(bodyObject)
    this[Headers.CONTENT_TYPE] = "application/json"
    return body(bodyString, charset)
}
