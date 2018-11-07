package com.github.kittinunf.fuel.core.extensions

import com.github.kittinunf.fuel.core.DefaultRequest
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Test
import java.net.URL

class FormattingTest {

    @Test
    fun httpGetCurlString() {
        val request = DefaultRequest(
                method = Method.GET,
                url = URL("http://httpbin.org/get"),
                headers = Headers.from("Authentication" to "Bearer xxx"),
                parameters = listOf("foo" to "xxx")
        )

        MatcherAssert.assertThat(request.cUrlString(), CoreMatchers.equalTo("curl -i -H \"Authentication:Bearer xxx\" http://httpbin.org/get"))
    }

    @Test
    fun httpPostCurlString() {
        val request = DefaultRequest(
                method = Method.POST,
                url = URL("http://httpbin.org/post"),
                headers = Headers.from("Authentication" to "Bearer xxx"),
                parameters = listOf("foo" to "xxx")
        )

        MatcherAssert.assertThat(request.cUrlString(), CoreMatchers.equalTo("curl -i -X POST -H \"Authentication:Bearer xxx\" http://httpbin.org/post"))
    }

    @Test
    fun httpStringWithOutParams() {
        val request = DefaultRequest(
                Method.GET,
                url = URL("http://httpbin.org/post"),
                headers = Headers.from("Content-Type" to "text/html")
        )

        MatcherAssert.assertThat(request.httpString(), CoreMatchers.startsWith("GET http"))
        MatcherAssert.assertThat(request.httpString(), CoreMatchers.containsString("Content-Type"))
    }

    @Test
    fun httpStringWithParams() {
        val request = DefaultRequest(
                Method.POST,
                url = URL("http://httpbin.org/post"),
                headers = Headers.from("Content-Type" to "text/html"),
                parameters = listOf("foo" to "xxx")
        ).body("it's a body")

        MatcherAssert.assertThat(request.httpString(), CoreMatchers.startsWith("POST http"))
        MatcherAssert.assertThat(request.httpString(), CoreMatchers.containsString("Content-Type"))
        MatcherAssert.assertThat(request.httpString(), CoreMatchers.containsString("body"))
    }
}