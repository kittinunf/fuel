package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.HeaderName
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.net.HttpURLConnection
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RequestHeaderTest : MockHttpTestCase() {
    @Test
    fun httpRequestHeader() {
        val manager = FuelManager()

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/get"),
            response = mock.reflect()
        )

        val headerKey = "Custom"
        val headerValue = "foobar"

        val (request, response, result) = manager.request(Method.GET, mock.path("get"))
                .header(headerKey to headerValue).response()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        val string = String(data as ByteArray)
        assertThat(string, containsString(headerKey))
        assertThat(string, containsString(headerValue))
    }

    @Test
    fun cookieHeaderUsingAppendIsCorrectlyCollapsed() {
        val manager = FuelManager()

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/get"),
            response = mock.reflect()
        )

        val (_, response, _) = manager.request(Method.GET, mock.path("get"))
                .appendHeader("foo" to "bar", "a" to "b", "cookie" to "val1=x", "cookie" to "val2=y", "cookie" to "val3=z", "cookie" to "val4=j")
                .responseString()

        assertThat("val1=x; val2=y; val3=z; val4=j", equalTo(response[Headers.COOKIE].lastOrNull()))
    }

    @Test
    fun headersCollapseCorrectly() {
        val manager = FuelManager()
        val request = manager.request(Method.GET, mock.path("get"))
            .appendHeader("foo" to "bar; p=2", "foo" to "baz", "cookie" to "val1=x", "cookie" to "val2=y", "cookie" to "val3=z", "cookie" to "val4=j")

        assertThat("val1=x; val2=y; val3=z; val4=j", equalTo(Headers.collapse(HeaderName(Headers.COOKIE), request[Headers.COOKIE])))
        assertThat("bar; p=2, baz", equalTo(Headers.collapse(HeaderName("foo"), request["foo"])))
    }

    @Test
    fun cookieHeaderUsingHeaderUsesLastValue() {
        val manager = FuelManager()
        val request = manager.request(Method.GET, mock.path("get"))
                .header("cookie" to "val1=x", "cookie" to "val2=y")

        assertThat("val2=y", equalTo(Headers.collapse(HeaderName(Headers.COOKIE), request[Headers.COOKIE])))
    }

    @Test
    fun headersAreNormalised() {
        val manager = FuelManager()
        val request = manager.request(Method.GET, mock.path("get"))
                .header("cookie" to "val1=x", "Cookie" to "val2=y")
                .appendHeader("cOoKie" to "val3=x")
                .header("content-type", "application/vnd.fuel.test.v1+json")
                .appendHeader("Content-Type", "text/html")

        assertThat("val2=y; val3=x", equalTo(Headers.collapse(HeaderName(Headers.COOKIE), request[Headers.COOKIE])))
        assertThat("text/html", equalTo(Headers.collapse(HeaderName(Headers.CONTENT_TYPE), request[Headers.CONTENT_TYPE])))
    }
}
