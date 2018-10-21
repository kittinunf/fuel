package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelManager
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
    fun httpRequestHeaderWithDuplicateValuesForCookieKey() {
        val manager = FuelManager()

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/get"),
            response = mock.reflect()
        )

        val (_, response, _) = manager.request(Method.GET, mock.path("get"))
                .header("foo" to "bar", "a" to "b", "cookie" to "val1=x", "cookie" to "val2=y", "cookie" to "val3=z", "cookie" to "val4=j")
                .responseString()

        assertThat("val1=x; val2=y; val3=z; val4=j", equalTo(response.headers["Cookie"]?.firstOrNull()))
    }

    @Test
    fun multipleHeadersByTheSameKeyWillBeCorrectlyFormatted() {
        val manager = FuelManager()
        val request = manager.request(Method.GET, mock.path("get"))
                .header("foo" to "bar", "a" to "b", "cookie" to "val1=x", "cookie" to "val2=y", "cookie" to "val3=z", "cookie" to "val4=j")

        assertThat("val1=x; val2=y; val3=z; val4=j", equalTo(request.headers["cookie"]))
        assertThat("bar", equalTo(request.headers["foo"]))
        assertThat("b", equalTo(request.headers["a"]))
    }

    @Test
    fun multipleHeadersByTheSameKeyWillShowLastUsingMap() {
        val manager = FuelManager()
        val request = manager.request(Method.GET, mock.path("get"))
                .header(mapOf("cookie" to "val1=x", "cookie" to "val2=y"), false)

        assertThat("val2=y", equalTo(request.headers["cookie"]))
    }
}
