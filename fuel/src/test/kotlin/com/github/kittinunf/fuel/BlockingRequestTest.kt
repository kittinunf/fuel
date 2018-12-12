package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.Encoding
import com.github.kittinunf.fuel.core.FileDataPart
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.test.MockHttpTestCase
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.io.File
import java.net.HttpURLConnection

class BlockingRequestTest : MockHttpTestCase() {
    private val manager: FuelManager by lazy { FuelManager() }

    class PathStringConvertibleImpl(url: String) : RequestFactory.PathStringConvertible {
        override val path = url
    }

    class RequestConvertibleImpl(val method: Method, private val url: String) : RequestFactory.RequestConvertible {
        override val request = createRequest()

        private fun createRequest(): Request {
            val encoder = Encoding(
                httpMethod = method,
                urlString = url,
                parameters = listOf("foo" to "bar")
            )
            return encoder.request
        }
    }

    @Test
    fun httpGetRequestWithDataResponse() {
        val httpRequest = mock.request()
            .withMethod(Method.GET.value)
            .withPath("/get")

        mock.chain(request = httpRequest, response = mock.reflect())

        val (request, response, data) = manager.request(Method.GET, mock.path("get")).response()
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, equalTo(statusCode))
    }

    @Test
    fun httpGetRequestWithStringResponse() {
        val httpRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mock.chain(request = httpRequest, response = mock.reflect())

        val (request, response, data) = manager.request(Method.GET, mock.path("get")).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, equalTo(statusCode))
    }

    @Test
    fun httpGetRequestWithParameters() {
        val paramKey = "foo"
        val paramValue = "bar"

        val httpRequest = mock.request()
            .withMethod(Method.GET.value)
            .withPath("/get")
                .withQueryStringParameter(paramKey, paramValue)

        mock.chain(request = httpRequest, response = mock.reflect())

        val (request, response, data) = manager.request(Method.GET, mock.path("get"), listOf(paramKey to paramValue)).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, equalTo(statusCode))

        assertThat(data.get(), containsString(paramKey))
        assertThat(data.get(), containsString(paramValue))
    }

    @Test
    fun httpPostRequestWithParameters() {
        val paramKey = "foo"
        val paramValue = "bar"

        val httpRequest = mock.request()
                .withMethod(Method.POST.value)
                .withPath("/post")
                .withBody("$paramKey=$paramValue")

        mock.chain(request = httpRequest, response = mock.reflect())

        val (request, response, data) = manager.request(Method.POST, mock.path("post"), listOf(paramKey to paramValue)).responseString()
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, equalTo(statusCode))

        assertThat(data.get(), containsString(paramKey))
        assertThat(data.get(), containsString(paramValue))
    }

    @Test
    fun httpPostRequestWithBody() {
        val foo = "foo"
        val bar = "bar"
        val body = "{ $foo : $bar }"

        val httpRequest = mock.request()
                .withMethod(Method.POST.value)
                .withPath("/post")

        mock.chain(request = httpRequest, response = mock.reflect())

        val (request, response, data) = manager.request(Method.POST, mock.path("post")).body(body).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, equalTo(statusCode))

        assertThat(data.get(), containsString(foo))
        assertThat(data.get(), containsString(bar))
    }

    @Test
    fun httpPutRequestWithParameters() {
        val paramKey = "foo"
        val paramValue = "bar"

        val httpRequest = mock.request()
                .withMethod(Method.PUT.value)
                .withPath("/put")
                .withBody("$paramKey=$paramValue")

        mock.chain(request = httpRequest, response = mock.reflect())

        val (request, response, data) = manager.request(Method.PUT, mock.path("put"), listOf(paramKey to paramValue)).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, equalTo(statusCode))

        assertThat(data.get(), containsString(paramKey))
        assertThat(data.get(), containsString(paramValue))
    }

    @Test
    fun httpDeleteRequestWithParameters() {
        val paramKey = "foo"
        val paramValue = "bar"

        val httpRequest = mock.request()
                .withMethod(Method.DELETE.value)
                .withPath("/delete")
                .withQueryStringParameter(paramKey, paramValue)

        mock.chain(request = httpRequest, response = mock.reflect())

        val (request, response, data) = manager.request(Method.DELETE, mock.path("delete"), listOf(paramKey to paramValue)).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, equalTo(statusCode))

        assertThat(data.get(), containsString(paramKey))
        assertThat(data.get(), containsString(paramValue))
    }

    @Test
    fun httpGetRequestWithPathStringConvertible() {
        val httpRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/path-string")

        mock.chain(request = httpRequest, response = mock.reflect())

        val (request, response, data) = manager.request(Method.GET, PathStringConvertibleImpl(mock.path("path-string"))).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, equalTo(statusCode))

        assertThat(data.get(), containsString("path-string"))
    }

    @Test
    fun httpGetRequestWithRequestConvertible() {
        val httpRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mock.chain(request = httpRequest, response = mock.reflect())

        val (request, response, data) = manager.request(RequestConvertibleImpl(Method.GET, mock.path("get"))).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, equalTo(statusCode))
    }

    @Test
    fun httpGetRequestWithPathStringConvertibleAndOverriddenParameters() {
        val paramKey = "foo"
        val paramValue = "xxx"

        val httpRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mock.chain(request = httpRequest, response = mock.reflect())

        val (request, response, data) = manager.request(Method.GET, PathStringConvertibleImpl(mock.path("get")), listOf(paramKey to paramValue)).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, equalTo(statusCode))

        assertThat(data.get(), containsString(paramKey))
        assertThat(data.get(), containsString(paramValue))
    }

    @Test
    fun httpGetRequestWithNotOverriddenHeaders() {
        val headerKey = Headers.CONTENT_TYPE
        val headerValue = "application/json"
        manager.baseHeaders = mapOf(headerKey to headerValue)

        val httpRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mock.chain(request = httpRequest, response = mock.reflect())

        val (request, response, data) = manager.request(Method.GET, mock.path("get"), listOf("email" to "foo@bar.com")).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, equalTo(statusCode))

        assertThat(request[headerKey].lastOrNull(), equalTo(headerValue))
    }

    @Test
    fun httpUploadRequestWithParameters() {
        val httpRequest = mock.request()
                .withMethod(Method.POST.value)
                .withPath("/upload")

        mock.chain(request = httpRequest, response = mock.reflect())

        val (request, response, data) =
                manager.upload(mock.path("upload"), parameters = listOf("foo" to "bar", "foo1" to "bar1"))
                    .add { FileDataPart.from(File(System.getProperty("user.dir")).resolve("/src/test/assets"), "lorem_ipsum_long.tmp") }
                    .responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, equalTo(statusCode))
    }
}
