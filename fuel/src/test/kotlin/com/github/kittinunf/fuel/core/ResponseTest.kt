package com.github.kittinunf.fuel.core

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.net.URL

class ResponseTest {
    @Test
    fun responseHasHeaderGetter() {
        val response = Response(
            url = URL("https://test.fuel.com"),
            headers = Headers.from(Headers.CONTENT_TYPE to "image/png")
        )

        assertThat(response[Headers.CONTENT_TYPE].lastOrNull(), equalTo("image/png"))
    }

    @Test
    fun responseHasHeader() {
        val response = Response(
            url = URL("https://test.fuel.com"),
            headers = Headers.from(Headers.CONTENT_TYPE to "image/png")
        )

        assertThat(response.header(Headers.CONTENT_TYPE).lastOrNull(), equalTo("image/png"))
    }
}