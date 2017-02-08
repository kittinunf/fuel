package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.Encoding
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.File
import java.net.HttpURLConnection
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class BlockingRequestTest : BaseTestCase() {

    val manager: FuelManager by lazy { FuelManager() }

    enum class HttpsBin(relativePath: String) : Fuel.PathStringConvertible {
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
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.httpStatusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpGetRequestWithStringResponse() {
        val (request, response, data) = manager.request(Method.GET, "http://httpbin.org/get").responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

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
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.httpStatusCode, isEqualTo(statusCode))

        assertThat(data.get(), containsString(paramKey))
        assertThat(data.get(), containsString(paramValue))
    }

    @Test
    fun httpPostRequestWithParameters() {
        val paramKey = "foo"
        val paramValue = "bar"

        val (request, response, data) = manager.request(Method.GET, "http://httpbin.org/get", listOf(paramKey to paramValue)).responseString()
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.httpStatusCode, isEqualTo(statusCode))

        assertThat(data.get(), containsString(paramKey))
        assertThat(data.get(), containsString(paramValue))
    }

    @Test
    fun httpPostRequestWithBody() {
        val foo = "foo"
        val bar = "bar"
        val body = "{ $foo : $bar }"

        val (request, response, data) = manager.request(Method.POST, "http://httpbin.org/post").body(body).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.httpStatusCode, isEqualTo(statusCode))

        assertThat(data.get(), containsString(foo))
        assertThat(data.get(), containsString(bar))
    }

    @Test
    fun httpPutRequestWithParameters() {
        val paramKey = "foo"
        val paramValue = "bar"

        val (request, response, data) = manager.request(Method.PUT, "http://httpbin.org/put", listOf(paramKey to paramValue)).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.httpStatusCode, isEqualTo(statusCode))

        assertThat(data.get(), containsString(paramKey))
        assertThat(data.get(), containsString(paramValue))
    }

    @Test
    fun httpDeleteRequestWithParameters() {
        val paramKey = "foo"
        val paramValue = "bar"

        val (request, response, data) = manager.request(Method.DELETE, "http://httpbin.org/delete", listOf(paramKey to paramValue)).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.httpStatusCode, isEqualTo(statusCode))

        assertThat(data.get(), containsString(paramKey))
        assertThat(data.get(), containsString(paramValue))
    }

    @Test
    fun httpGetRequestWithPathStringConvertible() {
        val (request, response, data) = manager.request(Method.GET, HttpsBin.USER_AGENT).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.httpStatusCode, isEqualTo(statusCode))

        assertThat(data.get(), containsString("user-agent"))
    }

    @Test
    fun httpGetRequestWithRequestConvertible() {
        val (request, response, data) = manager.request(HttpBinConvertible(Method.GET, "get")).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.httpStatusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpGetRequestWithRequestConvertibleAndOverriddenParameters() {
        val paramKey = "foo"
        val paramValue = "xxx"

        val (request, response, data) = manager.request(Method.POST, "http://httpbin.org/post", listOf(paramKey to paramValue)).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.httpStatusCode, isEqualTo(statusCode))

        assertThat(data.get(), containsString(paramKey))
        assertThat(data.get(), containsString(paramValue))
    }

    @Test
    fun httpGetRequestWithNotOverriddenHeaders() {
        val headerKey = "Content-Type"
        val headerValue = "application/json"
        manager.baseHeaders = mapOf(headerKey to headerValue)

        val (request, response, data) = manager.request(Method.POST, HttpsBin.POST, listOf("email" to "foo@bar.com")).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.httpStatusCode, isEqualTo(statusCode))

        assertThat(request.httpHeaders[headerKey], isEqualTo(headerValue))
    }

    @Test
    fun httpUploadRequestWithParameters() {
        val (request, response, data) =
                manager.upload(HttpsBin.POST.path, param = listOf("foo" to "bar", "foo1" to "bar1"))
                        .source { request, url ->
                            val dir = System.getProperty("user.dir")
                            val currentDir = File(dir, "src/test/assets")
                            File(currentDir, "lorem_ipsum_long.tmp")
                        }
                        .responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        println(data)
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.httpStatusCode, isEqualTo(statusCode))
    }

}
