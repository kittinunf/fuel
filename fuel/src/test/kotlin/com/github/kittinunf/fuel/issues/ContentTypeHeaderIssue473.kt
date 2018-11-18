package com.github.kittinunf.fuel.issues

import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.test.MockHttpTestCase
import com.github.kittinunf.fuel.test.MockReflected
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class ContentTypeHeaderIssue473 : MockHttpTestCase() {

    @Test
    fun jsonBodyContentTypeHeader() {
        val value = "{ \"foo\": \"bar\" }"
        val request = reflectedRequest(Method.POST, "json-body")
            .jsonBody(value)

        val (_, _, result) = request.responseObject(MockReflected.Deserializer())
        val (reflected, error) = result

        assertThat(error, CoreMatchers.nullValue())
        assertThat(reflected, CoreMatchers.notNullValue())

        val contentType = reflected!![Headers.CONTENT_TYPE]
        assertThat(contentType.lastOrNull(), equalTo("application/json"))
        assertThat(contentType.size, equalTo(1))
        assertThat(reflected.body?.string, equalTo(value))
    }
}