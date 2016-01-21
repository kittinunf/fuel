package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.*
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Test
import java.net.HttpURLConnection
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class BlockingRequestTest : BaseTestCase() {

    val manager: Manager by lazy { Manager() }

    enum class HttpsBin(val relativePath: String) : Fuel.PathStringConvertible {
        USER_AGENT("user-agent"),
        POST("post"),
        PUT("put"),
        DELETE("delete");

        override val path = "https://httpbin.org/$relativePath"
    }

    class HttpBinConvertible(val method: Method, val relativePath: String) : Fuel.RequestConvertible {
        override val request = createRequest()

        fun createRequest(): Request {
            val encoder = Encoding().apply {
                httpMethod = method
                urlString = "http://httpbin.org/$relativePath"
                parameters = listOf("foo" to "bar")
            }
            return encoder.request
        }
    }

    @Test
    fun httpGetRequestWithDataResponse() {
        val (request, response, data) = manager.request(Method.GET, "http://httpbin.org/get").response()
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.httpStatusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpGetRequestWithStringResponse() {
        val (request, response, data) = manager.request(Method.GET, "http://httpbin.org/get").responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.httpStatusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpGetRequestWithParameters() {
        val paramKey = "foo"
        val paramValue = "bar"

        val (request, response, data) = manager.request(Method.GET, "http://httpbin.org/get", listOf(paramKey to paramValue)).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.httpStatusCode, isEqualTo(statusCode))

        assertThat(data, containsString(paramKey))
        assertThat(data, containsString(paramValue))
    }

    @Test
    fun httpPostRequestWithParameters() {
        val paramKey = "foo"
        val paramValue = "bar"

        val (request, response, data) = manager.request(Method.GET, "http://httpbin.org/get", listOf(paramKey to paramValue)).responseString()
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.httpStatusCode, isEqualTo(statusCode))

        assertThat(data, containsString(paramKey))
        assertThat(data, containsString(paramValue))
    }

    @Test
    fun httpPostRequestWithBody() {
        val foo = "foo"
        val bar = "bar"
        val body = "{ $foo : $bar }"

        val (request, response, data) = manager.request(Method.POST, "http://httpbin.org/post").body(body).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.httpStatusCode, isEqualTo(statusCode))

        assertThat(data, containsString(foo))
        assertThat(data, containsString(bar))
    }

    @Test
    fun httpPutRequestWithParameters() {
        val paramKey = "foo"
        val paramValue = "bar"

        val (request, response, data) = manager.request(Method.PUT, "http://httpbin.org/put", listOf(paramKey to paramValue)).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.httpStatusCode, isEqualTo(statusCode))

        assertThat(data, containsString(paramKey))
        assertThat(data, containsString(paramValue))
    }

    @Test
    fun httpDeleteRequestWithParameters() {
        val paramKey = "foo"
        val paramValue = "bar"

        val (request, response, data) = manager.request(Method.DELETE, "http://httpbin.org/delete", listOf(paramKey to paramValue)).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.httpStatusCode, isEqualTo(statusCode))

        assertThat(data, containsString(paramKey))
        assertThat(data, containsString(paramValue))
    }

    @Test
    fun httpGetRequestWithPathStringConvertible() {
        val (request, response, data) = manager.request(Method.GET, HttpsBin.USER_AGENT).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.httpStatusCode, isEqualTo(statusCode))

        assertThat(data, containsString("user-agent"))
    }

    @Test
    fun httpGetRequestWithRequestConvertible() {
        val (request, response, data) = manager.request(HttpBinConvertible(Method.GET, "get")).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.httpStatusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpGetRequestWithRequestConvertibleAndOverriddenParameters() {
        val paramKey = "foo"
        val paramValue = "xxx"

        val (request, response, data) =manager.request(Method.POST, "http://httpbin.org/post", listOf(paramKey to paramValue)).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.httpStatusCode, isEqualTo(statusCode))

        assertThat(data, containsString(paramKey))
        assertThat(data, containsString(paramValue))
    }

}

