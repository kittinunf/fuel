package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.StringContains.containsString
import org.junit.Test
import java.net.URL

class DefaultRequestTest {

    @Test
    fun requestToStringIncludesMethod() {
        val request = DefaultRequest(
            Method.POST,
            url = URL("http://httpbin.org/post"),
            headers = Headers.from("Content-Type" to "text/html"),
            parameters = listOf("foo" to "xxx")
        ).body("it's a body")

        val printed = request.toString()
        assertThat(printed, containsString("-->"))
        assertThat(printed, containsString("POST"))
        assertThat(printed, containsString("http://httpbin.org/post"))
        assertThat(printed, containsString("it's a body"))
        assertThat(printed, containsString("Content-Type"))
        assertThat(printed, containsString("text/html"))
    }
}
