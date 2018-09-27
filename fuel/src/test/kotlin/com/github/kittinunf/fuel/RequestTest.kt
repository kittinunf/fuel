package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.fuel.util.decodeBase64
import com.google.common.net.MediaType
import junit.framework.TestCase.assertEquals
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test
import org.mockserver.model.BinaryBody
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RequestTest : MockHttpTestCase() {
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
    fun testResponseURLShouldSameWithRequestURL() {
        mockChain(
            request = mockRequest().withMethod(Method.GET.value).withPath("/request"),
            response = mockReflect()
        )

        val (request, response, result) = manager.request(Method.GET, mockPath("request")).response()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(request.url, notNullValue())
        assertThat(response.url, notNullValue())
        assertThat(request.url, isEqualTo(response.url))
    }

    @Test
    fun httpGetRequestWithDataResponse() {
        mockChain(
            request = mockRequest().withMethod(Method.GET.value).withPath("/request"),
            response = mockReflect()
        )

        val (request, response, result) = manager.request(Method.GET, mockPath("request")).response()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpGetRequestWithStringResponse() {
        mockChain(
            request = mockRequest().withMethod(Method.GET.value).withPath("/request"),
            response = mockReflect()
        )

        val (request, response, result) = manager.request(Method.GET, mockPath("request")).responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpGetRequestWithImageResponse() {
        val decodedImage = "iVBORwKGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAClEQVQYV2NgYAAAAAMAAWgmWQAAAAASUVORK5CYII=".decodeBase64()

        val httpResponse = mockResponse()
                .withHeader("Content-Type", "image/png")
                .withBody(BinaryBody(decodedImage))

        mockChain(
            request = mockRequest().withMethod(Method.GET.value).withPath("/image"),
            response = httpResponse
        )

        val (request, response, result) = manager.request(Method.GET, mockPath("image")).responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.toString(), containsString("bytes of image/png"))

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpGetRequestWithBytesResponse() {
        val bytes = ByteArray(555)
        Random().nextBytes(bytes)

        mockChain(
            request = mockRequest().withMethod(Method.GET.value).withPath("/bytes"),
            response = mockResponse().withBody(BinaryBody(bytes, MediaType.OCTET_STREAM))
        )

        val (request, response, result) = manager.request(Method.GET, mockPath("bytes")).responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.toString(), containsString("Body : (555 bytes of application/octet-stream)"))

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun testProcessBodyWithUnknownContentTypeAndNoData() {
        val bytes = ByteArray(555)
        Random().nextBytes(bytes)

        mockChain(
            request = mockRequest().withMethod(Method.GET.value).withPath("/bytes"),
            response = mockResponse().withBody(BinaryBody(bytes, MediaType.OCTET_STREAM))
        )

        val (request, response, result) = manager.request(Method.GET, mockPath("bytes")).responseString()
        val (_, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())

        assertThat(response.processBody("(unknown)", ByteArray(0)), isEqualTo("(empty)"))
    }

    @Test
    fun testGuessContentTypeWithNoContentTypeInHeaders() {
        val bytes = ByteArray(555)
        Random().nextBytes(bytes)

        mockChain(
            request = mockRequest().withMethod(Method.GET.value).withPath("/bytes"),
            response = mockResponse().withBody(BinaryBody(bytes, null)).withHeader("Content-Type", "")
        )

        val (request, response, result) = manager.request(Method.GET, mockPath("bytes")).responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val headers: Map<String, List<String>> = mapOf(Pair("Content-Type", listOf("")))
        assertThat(response.guessContentType(headers), isEqualTo("(unknown)"))
    }

    // Needs Better PNG Base64 in same line
    /*@Test
    fun testGuessContentTypeWithNoHeaders() {
        val decodedImage = Base64.decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVQYV2NgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII=", Base64.DEFAULT)

        val httpResponse = mockResponse()
                .withHeader("Content-Type", "")
                .withBody(BinaryBody(decodedImage))

        mockChain(
            request = mockRequest().withMethod(Method.GET.value).withPath("/image"),
            response = httpResponse
        )

        val (request, response, result) = manager.request(Method.GET, mockPath("image")).responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val headers: Map<String, List<String>> = mapOf(Pair("Content-Type", listOf("")))
        assertThat(response.guessContentType(headers), isEqualTo("image/png"))
    }*/

    @Test
    fun httpGetRequestWithParameters() {
        val paramKey = "foo"
        val paramValue = "bar"

        mockChain(
            request = mockRequest().withMethod(Method.GET.value).withPath("/get"),
            response = mockReflect()
        )

        val (request, response, result) = manager.request(Method.GET, mockPath("get"), listOf(paramKey to paramValue)).responseString()
        val (data, error) = result
        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        assertThat(string, containsString(paramKey))
        assertThat(string, containsString(paramValue))
    }

    @Test
    fun httpPostRequestWithParameters() {
        val paramKey = "foo"
        val paramValue = "bar"

        mockChain(
            request = mockRequest().withMethod(Method.POST.value).withPath("/post"),
            response = mockReflect()
        )

        val (request, response, result) = manager.request(Method.POST, mockPath("post"), listOf(paramKey to paramValue)).responseString()
        val (data, error) = result

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        assertThat(string, containsString(paramKey))
        assertThat(string, containsString(paramValue))
    }

    @Test
    fun httpPostRequestWithBody() {
        val foo = "foo"
        val bar = "bar"
        val body = "{ $foo : $bar }"

        // Reflect encodes the body as a string, and gives back the body as a property of the body
        //  therefore the outer body here is the ey and the inner string is the actual body
        val correctBodyResponse = "\"body\":{\"type\":\"STRING\",\"string\":\"$body\",\"contentType\":\"text/plain; charset=utf-8\"}"

        mockChain(
            request = mockRequest().withMethod(Method.POST.value).withPath("/post"),
            response = mockReflect()
        )

        val (request, response, result) = manager.request(Method.POST, mockPath("post"))
                .jsonBody(body)
                .responseString()
        val (data, error) = result

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        assertThat(string, containsString(correctBodyResponse))
    }

    @Test
    fun httpPutRequestWithParameters() {
        val paramKey = "foo"
        val paramValue = "bar"

        mockChain(
            request = mockRequest().withMethod(Method.PUT.value).withPath("/put"),
            response = mockReflect()
        )

        val (request, response, result) = manager.request(Method.PUT, mockPath("put"), listOf(paramKey to paramValue)).responseString()
        val (data, error) = result

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        assertThat(string, containsString(paramKey))
        assertThat(string, containsString(paramValue))
    }

    @Test
    fun httpPatchRequestWithParameters() {
        val paramKey = "foo2"
        val paramValue = "bar2"

        mockChain(
            request = mockRequest().withMethod(Method.PATCH.value).withPath("/patch"),
            response = mockReflect()
        )

        mockChain(
            request = mockRequest().withMethod(Method.POST.value).withHeader("X-HTTP-Method-Override", Method.PATCH.value).withPath("/patch"),
            response = mockReflect()
        )

        val (request, response, result) = manager.request(Method.PATCH, mockPath("patch"), listOf(paramKey to paramValue)).responseString()
        val (data, error) = result

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        assertThat(string, containsString(paramKey))
        assertThat(string, containsString(paramValue))
    }

    @Test
    fun httpDeleteRequestWithParameters() {
        val paramKey = "foo"
        val paramValue = "bar"
        val foo = "foo"
        val bar = "bar"
        val body = "{ $foo : $bar }"
        val correctBodyResponse = "\"body\":{\"type\":\"STRING\",\"string\":\"$body\",\"contentType\":\"text/plain; charset=utf-8\"}"

        mockChain(
            request = mockRequest().withMethod(Method.DELETE.value).withPath("/delete"),
            response = mockReflect()
        )

        val (request, response, result) = manager.request(Method.DELETE, mockPath("delete"), listOf(paramKey to paramValue))
                .jsonBody(body)
                .responseString()
        val (data, error) = result

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        assertThat(string, containsString(paramKey))
        assertThat(string, containsString(paramValue))
        assertThat(string, containsString(correctBodyResponse))
    }

    @Test
    fun httpHeadRequest() {
        val paramKey = "foo"
        val paramValue = "bar"

        mockChain(
            request = mockRequest().withMethod(Method.HEAD.value).withPath("/head"),
            response = mockResponse().withStatusCode(HttpURLConnection.HTTP_OK)
        )

        val (request, response, result) = manager.request(Method.HEAD, mockPath("head"), listOf(paramKey to paramValue)).responseString()
        val (data, error) = result

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))

        assertThat(string, equalTo(""))
    }

    @Test
    fun httpOptionsRequest() {
        mockChain(
            request = mockRequest().withMethod(Method.OPTIONS.value).withPath("/options"),
            response = mockResponse().withStatusCode(HttpURLConnection.HTTP_OK)
        )

        val (request, response, result) = manager.request(Method.OPTIONS, mockPath("options")).responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
    }

    @Test
    fun httpTraceRequest() {
        mockChain(
            request = mockRequest().withMethod(Method.TRACE.value).withPath("/trace"),
            response = mockResponse().withStatusCode(HttpURLConnection.HTTP_OK)
        )

        val (request, response, result) = manager.request(Method.TRACE, mockPath("trace")).responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
    }

    @Test
    fun httpGetRequestUserAgentWithPathStringConvertible() {
        mockChain(
            request = mockRequest().withMethod(Method.GET.value).withPath("/user-agent"),
            response = mockReflect()
        )

        val (request, response, result) = manager.request(Method.GET, PathStringConvertibleImpl(mockPath("user-agent"))).responseString()
        val (data, error) = result

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))

        assertThat(string, containsString("user-agent"))
    }

    @Test
    fun httpGetRequestWithRequestConvertible() {
        mockChain(
            request = mockRequest().withMethod(Method.GET.value).withPath("/get"),
            response = mockReflect()
        )

        val (request, response, result) = manager.request(RequestConvertibleImpl(Method.GET, mockPath("get"))).responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
    }

    @Test
    fun httpPatchRequestWithRequestConvertible() {
        val paramKey = "foo"
        val paramValue = "bar"

        mockChain(
            request = mockRequest().withMethod(Method.PATCH.value).withPath("/patch"),
            response = mockReflect()
        )

        // HttpUrlConnection doesn't support patch
        mockChain(
            request = mockRequest().withMethod(Method.POST.value).withHeader("X-HTTP-Method-Override", Method.PATCH.value).withPath("/patch"),
            response = mockReflect()
        )

        val (request, response, result) = manager.request(Method.PATCH, PathStringConvertibleImpl(mockPath("patch")), listOf(paramKey to paramValue)).responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
    }

    @Test
    fun httpPostRequestWithRequestConvertibleAndOverriddenParameters() {
        val paramKey = "foo"
        val paramValue = "xxx"

        mockChain(
            request = mockRequest().withMethod(Method.POST.value).withPath("/post"),
            response = mockReflect()
        )

        val (request, response, result) = manager.request(Method.POST, mockPath("post"), listOf(paramKey to paramValue)).responseString()
        val (data, error) = result

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        assertThat(string, containsString(paramKey))
        assertThat(string, containsString(paramValue))
    }

    // @Test
    // TODO turn on when it works reliably
    fun httpGetRequestCancel() {
        regularMode {
            /*
                TODO: turn into mocked request. This one is failing because the mock server probably
                    doesn't allow for streamed responses. Or maybe something else. Does show the
                    issue with completed requests that are cancelled. Should probably be killed even
                    if they are completed.


            val bytes = ByteArray(1024 * 1024)
            Random().nextBytes(bytes)

            mockChain(
                request = mockRequest().withMethod(Method.GET.value).withPath("/bytes"),
                response = mockResponse().withBody(BinaryBody(bytes, MediaType.OCTET_STREAM))
            )

            val file = File.createTempFile(bytes.toString(), null)
            val requestPrimed = manager.download(mockPath("bytes")).destination { _, _ -> file }

            requestPrimed.progress { _, _ ->
                requestPrimed.cancel()
            }

            val (request, response, result) = requestPrimed.response()
            val (data, error) = result

            assertThat(request, notNullValue())
            assertThat(response, nullValue())
            assertThat(data, nullValue())
            assertThat(error, nullValue())

            */

            val (request, response, result) = manager.request(Method.GET, "http://httpbin.org/stream-bytes/4194304").responseString()
            request.cancel()

            // TODO: investigate. By this time there is already data loaded and cancel doesn't
            //  actually cancel anything. See comments next to assertions below:

            val (data, error) = result

            assertThat(request, notNullValue())
            assertThat(response.contentLength, isEqualTo(-1L)) // this is true
            assertThat(data, nullValue()) // but then this is not
            assertThat(error, nullValue())
        }
    }

    @Test
    fun httpGetCurlString() {
        val request = Request(method = Method.GET,
                path = "",
                url = URL("http://httpbin.org/get"),
                headers = mutableMapOf("Authentication" to "Bearer xxx"),
                parameters = listOf("foo" to "xxx"),
                timeoutInMillisecond = 15000,
                timeoutReadInMillisecond = 15000)

        assertThat(request.cUrlString(), isEqualTo("$ curl -i -H \"Authentication:Bearer xxx\" http://httpbin.org/get"))
    }

    @Test
    fun httpPostCurlString() {
        val request = Request(method = Method.POST,
                path = "",
                url = URL("http://httpbin.org/post"),
                headers = mutableMapOf("Authentication" to "Bearer xxx"),
                parameters = listOf("foo" to "xxx"),
                timeoutInMillisecond = 15000,
                timeoutReadInMillisecond = 15000)

        assertThat(request.cUrlString(), isEqualTo("$ curl -i -X POST -H \"Authentication:Bearer xxx\" http://httpbin.org/post"))
    }

    @Test
    fun httpStringWithOutParams() {
        val request = Request(Method.GET, "",
                url = URL("http://httpbin.org/post"),
                headers = mutableMapOf("Content-Type" to "text/html"),
                timeoutInMillisecond = 15000,
                timeoutReadInMillisecond = 15000)

        assertThat(request.httpString(), startsWith("GET http"))
        assertThat(request.httpString(), containsString("Content-Type"))
    }

    @Test
    fun httpStringWithParams() {
        val request = Request(Method.POST, "",
                url = URL("http://httpbin.org/post"),
                headers = mutableMapOf("Content-Type" to "text/html"),
                parameters = listOf("foo" to "xxx"),
                timeoutInMillisecond = 15000,
                timeoutReadInMillisecond = 15000).body("it's a body")

        assertThat(request.httpString(), startsWith("POST http"))
        assertThat(request.httpString(), containsString("Content-Type"))
        assertThat(request.httpString(), containsString("body"))
    }

    @Test
    fun httpGetParameterArrayWillFormCorrectURL() {
        val lionel = "Lionel Ritchie"
        val list = arrayOf("once", "Twice", "Three", "Times", "Lady")
        val params = listOf("foo" to list, "bar" to lionel)

        mockChain(
            request = mockRequest().withMethod(Method.GET.value).withPath("/get"),
            response = mockReflect()
        )

        val (response, error) = mockPath("get").httpGet(params).responseString().third
        val json = JSONObject(response)
        val query = json.getJSONObject("query")

        assertThat(error, nullValue())
        assertEquals(JSONArray("[\"$lionel\"]").toString(), query.getJSONArray("bar").toString())
        assertEquals(list.toList(), query.getJSONArray("foo[]").map { it.toString() })
    }
}

