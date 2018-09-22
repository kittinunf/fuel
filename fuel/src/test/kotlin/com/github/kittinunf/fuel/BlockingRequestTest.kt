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

class BlockingRequestTest : MockHttpTestCase() {
    private val manager: FuelManager by lazy { FuelManager() }

    class PathStringConvertibleImpl(url: String) : Fuel.PathStringConvertible {
        override val path = url
    }

    class RequestConvertibleImpl(val method: Method, private val url: String) : Fuel.RequestConvertible {
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
        val httpRequest = mockRequest()
            .withMethod(Method.GET.value)
            .withPath("/get")

        mockChain(request = httpRequest, response = mockReflect())

        val (request, response, data) = manager.request(Method.GET, mockPath("get")).response()
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpGetRequestWithStringResponse() {
        val httpRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mockChain(request = httpRequest, response = mockReflect())

        val (request, response, data) = manager.request(Method.GET, mockPath("get")).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpGetRequestWithParameters() {
        val paramKey = "foo"
        val paramValue = "bar"

        val httpRequest = mockRequest()
            .withMethod(Method.GET.value)
            .withPath("/get")
                .withQueryStringParameter(paramKey, paramValue)

        mockChain(request = httpRequest, response = mockReflect())

        val (request, response, data) = manager.request(Method.GET, mockPath("get"), listOf(paramKey to paramValue)).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        assertThat(data.get(), containsString(paramKey))
        assertThat(data.get(), containsString(paramValue))
    }

    @Test
    fun httpPostRequestWithParameters() {
        val paramKey = "foo"
        val paramValue = "bar"

        val httpRequest = mockRequest()
                .withMethod(Method.POST.value)
                .withPath("/post")
                .withBody("$paramKey=$paramValue")

        mockChain(request = httpRequest, response = mockReflect())

        val (request, response, data) = manager.request(Method.POST, mockPath("post"), listOf(paramKey to paramValue)).responseString()
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        assertThat(data.get(), containsString(paramKey))
        assertThat(data.get(), containsString(paramValue))
    }

    @Test
    fun httpPostRequestWithBody() {
        val foo = "foo"
        val bar = "bar"
        val body = "{ $foo : $bar }"

        val httpRequest = mockRequest()
                .withMethod(Method.POST.value)
                .withPath("/post")

        mockChain(request = httpRequest, response = mockReflect())

        val (request, response, data) = manager.request(Method.POST, mockPath("post")).body(body).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        assertThat(data.get(), containsString(foo))
        assertThat(data.get(), containsString(bar))
    }

    @Test
    fun httpPutRequestWithParameters() {
        val paramKey = "foo"
        val paramValue = "bar"

        val httpRequest = mockRequest()
                .withMethod(Method.PUT.value)
                .withPath("/put")
                .withBody("$paramKey=$paramValue")

        mockChain(request = httpRequest, response = mockReflect())

        val (request, response, data) = manager.request(Method.PUT, mockPath("put"), listOf(paramKey to paramValue)).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        assertThat(data.get(), containsString(paramKey))
        assertThat(data.get(), containsString(paramValue))
    }

    @Test
    fun httpDeleteRequestWithParameters() {
        val paramKey = "foo"
        val paramValue = "bar"

        val httpRequest = mockRequest()
                .withMethod(Method.DELETE.value)
                .withPath("/delete")
                .withQueryStringParameter(paramKey, paramValue)

        mockChain(request = httpRequest, response = mockReflect())

        val (request, response, data) = manager.request(Method.DELETE, mockPath("delete"), listOf(paramKey to paramValue)).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        assertThat(data.get(), containsString(paramKey))
        assertThat(data.get(), containsString(paramValue))
    }

    @Test
    fun httpGetRequestWithPathStringConvertible() {
        val httpRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/path-string")

        mockChain(request = httpRequest, response = mockReflect())

        val (request, response, data) = manager.request(Method.GET, PathStringConvertibleImpl(mockPath("path-string"))).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        assertThat(data.get(), containsString("path-string"))
    }

    @Test
    fun httpGetRequestWithRequestConvertible() {
        val httpRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mockChain(request = httpRequest, response = mockReflect())

        val (request, response, data) = manager.request(RequestConvertibleImpl(Method.GET, mockPath("get"))).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpGetRequestWithPathStringConvertibleAndOverriddenParameters() {
        val paramKey = "foo"
        val paramValue = "xxx"

        val httpRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mockChain(request = httpRequest, response = mockReflect())

        val (request, response, data) = manager.request(Method.GET, PathStringConvertibleImpl(mockPath("get")), listOf(paramKey to paramValue)).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        assertThat(data.get(), containsString(paramKey))
        assertThat(data.get(), containsString(paramValue))
    }

    @Test
    fun httpGetRequestWithNotOverriddenHeaders() {
        val headerKey = "Content-Type"
        val headerValue = "application/json"
        manager.baseHeaders = mapOf(headerKey to headerValue)

        val httpRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mockChain(request = httpRequest, response = mockReflect())

        val (request, response, data) = manager.request(Method.GET, mockPath("get"), listOf("email" to "foo@bar.com")).responseString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        assertThat(request.headers[headerKey], isEqualTo(headerValue))
    }

    @Test
    fun httpUploadRequestWithParameters() {
        val httpRequest = mockRequest()
                .withMethod(Method.POST.value)
                .withPath("/upload")

        mockChain(request = httpRequest, response = mockReflect())

        val (request, response, data) =
                manager.upload(mockPath("upload"), param = listOf("foo" to "bar", "foo1" to "bar1"))
                        .source { _, _ ->
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
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

}
