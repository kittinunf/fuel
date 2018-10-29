package com.github.kittinunf.fuel.issues

import com.github.kittinunf.fuel.MockHttpTestCase
import com.github.kittinunf.fuel.MockReflected
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.util.UUID

class ContentTypeHeaderIssue408 : MockHttpTestCase() {

    @Test
    fun headerContentTypePreserved() {
        val tokenId = UUID.randomUUID()
        val request = reflectedRequest(Method.GET, "json/sessions",
            parameters = listOf("_action" to "getSessionInfo", "tokenId" to tokenId))
            .header("Accept-API-Version" to "resource=2.0")
            .header("Content-Type" to "application/json")

        val (_, _, result) = request.responseObject(MockReflected.Deserializer())
        val (reflected, error) = result

        assertThat(error, CoreMatchers.nullValue())
        assertThat(reflected, CoreMatchers.notNullValue())

        val contentType = reflected!![Headers.CONTENT_TYPE]
        assertThat(contentType.lastOrNull(), equalTo("application/json"))
        assertThat(contentType.size, equalTo(1))
    }
}
