package com.github.kittinunf.fuel.core.extensions

import com.github.kittinunf.fuel.core.requests.DefaultRequest
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.net.URL

class JsonBodyTest {
    @Test
    fun setsBodyCorrectly() {
        val body = "[42, { \"foo\": \"bar\" }]"
        val request = DefaultRequest(Method.POST, URL("https://test.fuel.com/body"))
            .jsonBody(body)

        assertThat(body, equalTo(String(request.body.toByteArray())))
    }

    @Test
    fun setsContentTypeCorrectly() {
        val request = DefaultRequest(Method.POST, URL("https://test.fuel.com/body"))
            .jsonBody("[42, { \"foo\": \"bar\" }]")

        assertThat(request[Headers.CONTENT_TYPE].lastOrNull(), equalTo("application/json"))
    }
}