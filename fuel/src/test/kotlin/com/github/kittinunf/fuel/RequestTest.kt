package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.DefaultRequest
import com.github.kittinunf.fuel.core.Encoding
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.extensions.cUrlString
import com.github.kittinunf.fuel.core.extensions.httpString
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.test.MockHttpTestCase
import com.github.kittinunf.fuel.util.decodeBase64
import com.google.common.net.MediaType
import junit.framework.TestCase.assertEquals
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.CoreMatchers.startsWith
import org.hamcrest.MatcherAssert.assertThat
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test
import org.mockserver.model.BinaryBody
import java.net.HttpURLConnection
import java.net.URL
import java.util.Random
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
        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/request"),
            response = mock.reflect()
        )

        val (request, response, result) = manager.request(Method.GET, mock.path("request")).response()
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
        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/request"),
            response = mock.reflect()
        )

        val (request, response, result) = manager.request(Method.GET, mock.path("request")).response()
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
        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/request"),
            response = mock.reflect()
        )

        val (request, response, result) = manager.request(Method.GET, mock.path("request")).responseString()
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

        val httpResponse = mock.response()
                .withHeader(Headers.CONTENT_TYPE, "image/png")
                .withBody(BinaryBody(decodedImage))

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/image"),
            response = httpResponse
        )

        val (request, response, result) = manager.request(Method.GET, mock.path("image")).responseString()
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

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/bytes"),
            response = mock.response().withBody(BinaryBody(bytes, MediaType.OCTET_STREAM))
        )

        val (request, response, result) = manager.request(Method.GET, mock.path("bytes")).responseString()
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

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/bytes"),
            response = mock.response().withBody(BinaryBody(bytes, MediaType.OCTET_STREAM))
        )

        val (request, response, result) = manager.request(Method.GET, mock.path("bytes")).responseString()
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

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/bytes"),
            response = mock.response().withBody(BinaryBody(bytes, null)).withHeader("Content-Type", "")
        )

        val (request, response, result) = manager.request(Method.GET, mock.path("bytes")).responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val headers: Headers = Headers.from(mapOf(Pair(Headers.CONTENT_TYPE, listOf(""))))
        assertThat(response.guessContentType(headers), isEqualTo("(unknown)"))
    }

    // Needs Better PNG Base64 in same line
    /*@Test
    fun testGuessContentTypeWithNoHeaders() {
        val decodedImage = Base64.decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVQYV2NgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII=", Base64.DEFAULT)

        val httpResponse = mock.response()
                .withHeader("Content-Type", "")
                .withBody(BinaryBody(decodedImage))

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/image"),
            response = httpResponse
        )

        val (request, response, result) = manager.request(Method.GET, mock.path("image")).responseString()
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

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/get"),
            response = mock.reflect()
        )

        val (request, response, result) = manager.request(Method.GET, mock.path("get"), listOf(paramKey to paramValue)).responseString()
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

        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/post"),
            response = mock.reflect()
        )

        val (request, response, result) = manager.request(Method.POST, mock.path("post"), listOf(paramKey to paramValue)).responseString()
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
        val body = "{ $foo: $bar }"

        // Reflect encodes the body as a string, and gives back the body as a property of the body
        //  therefore the outer body here is the ey and the inner string is the actual body
        val correctBodyResponse = "\"body\":{\"type\":\"STRING\",\"string\":\"$body\",\"contentType\":\"text/plain; charset=utf-8\"}"

        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/post"),
            response = mock.reflect()
        )

        val (request, response, result) = manager.request(Method.POST, mock.path("post"))
            .jsonBody(body)
            .responseString()
        val (data, error) = result

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(request["Content-Type"].lastOrNull(), equalTo("application/json"))
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

        mock.chain(
            request = mock.request().withMethod(Method.PUT.value).withPath("/put"),
            response = mock.reflect()
        )

        val (request, response, result) = manager.request(Method.PUT, mock.path("put"), listOf(paramKey to paramValue)).responseString()
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

        mock.chain(
            request = mock.request().withMethod(Method.PATCH.value).withPath("/patch"),
            response = mock.reflect()
        )

        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withHeader("X-HTTP-Method-Override", Method.PATCH.value).withPath("/patch"),
            response = mock.reflect()
        )

        val (request, response, result) = manager.request(Method.PATCH, mock.path("patch"), listOf(paramKey to paramValue)).responseString()
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

        mock.chain(
            request = mock.request().withMethod(Method.DELETE.value).withPath("/delete"),
            response = mock.reflect()
        )

        val (request, response, result) = manager.request(Method.DELETE, mock.path("delete"), listOf(paramKey to paramValue))
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

        mock.chain(
            request = mock.request().withMethod(Method.HEAD.value).withPath("/head"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_OK)
        )

        val (request, response, result) = manager.request(Method.HEAD, mock.path("head"), listOf(paramKey to paramValue)).responseString()
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
        mock.chain(
            request = mock.request().withMethod(Method.OPTIONS.value).withPath("/options"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_OK)
        )

        val (request, response, result) = manager.request(Method.OPTIONS, mock.path("options")).responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
    }

    @Test
    fun httpTraceRequest() {
        mock.chain(
            request = mock.request().withMethod(Method.TRACE.value).withPath("/trace"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_OK)
        )

        val (request, response, result) = manager.request(Method.TRACE, mock.path("trace")).responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
    }

    @Test
    fun httpGetRequestUserAgentWithPathStringConvertible() {
        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/user-agent"),
            response = mock.reflect()
        )

        val (request, response, result) = manager.request(Method.GET, PathStringConvertibleImpl(mock.path("user-agent"))).responseString()
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
        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/get"),
            response = mock.reflect()
        )

        val (request, response, result) = manager.request(RequestConvertibleImpl(Method.GET, mock.path("get"))).responseString()
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

        mock.chain(
            request = mock.request().withMethod(Method.PATCH.value).withPath("/patch"),
            response = mock.reflect()
        )

        // HttpUrlConnection doesn't support patch
        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withHeader("X-HTTP-Method-Override", Method.PATCH.value).withPath("/patch"),
            response = mock.reflect()
        )

        val (request, response, result) = manager.request(Method.PATCH, PathStringConvertibleImpl(mock.path("patch")), listOf(paramKey to paramValue)).responseString()
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

        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/post"),
            response = mock.reflect()
        )

        val (request, response, result) = manager.request(Method.POST, mock.path("post"), listOf(paramKey to paramValue)).responseString()
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
            /*
                TODO: turn into mocked request. This one is failing because the mock server probably
                    doesn't allow for streamed responses. Or maybe something else. Does show the
                    issue with completed requests that are cancelled. Should probably be killed even
                    if they are completed.


            val bytes = ByteArray(1024 * 1024)
            Random().nextBytes(bytes)

            mock.chain(
                request = mock.request().withMethod(Method.GET.value).withPath("/bytes"),
                response = mock.response().withBody(BinaryBody(bytes, MediaType.OCTET_STREAM))
            )

            val file = File.createTempFile(bytes.toString(), null)
            val requestPrimed = manager.download(mock.path("bytes")).destination { _, _ -> file }

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
            // request.cancel()

            // TODO: investigate. By this time there is already data loaded and cancel doesn't
            //  actually cancel anything. See comments next to assertions below:

            val (data, error) = result

            assertThat(request, notNullValue())
            assertThat(response.contentLength, isEqualTo(-1L)) // this is true
            assertThat(data, nullValue()) // but then this is not
            assertThat(error, nullValue())
    }

    @Test
    fun httpGetCurlString() {
        val request = DefaultRequest(
            method = Method.GET,
            url = URL("http://httpbin.org/get"),
            headers = Headers.from("Authentication" to "Bearer xxx"),
            parameters = listOf("foo" to "xxx")
        )

        assertThat(request.cUrlString(), isEqualTo("curl -i -H \"Authentication:Bearer xxx\" http://httpbin.org/get"))
    }

    @Test
    fun httpPostCurlString() {
        val request = DefaultRequest(
            method = Method.POST,
            url = URL("http://httpbin.org/post"),
            headers = Headers.from("Authentication" to "Bearer xxx"),
            parameters = listOf("foo" to "xxx")
        )

        assertThat(request.cUrlString(), isEqualTo("curl -i -X POST -H \"Authentication:Bearer xxx\" http://httpbin.org/post"))
    }

    @Test
    fun httpStringWithOutParams() {
        val request = DefaultRequest(
            Method.GET,
            url = URL("http://httpbin.org/post"),
            headers = Headers.from("Content-Type" to "text/html")
        )

        assertThat(request.httpString(), startsWith("GET http"))
        assertThat(request.httpString(), containsString("Content-Type"))
    }

    @Test
    fun httpStringWithParams() {
        val request = DefaultRequest(
            Method.POST,
            url = URL("http://httpbin.org/post"),
            headers = Headers.from("Content-Type" to "text/html"),
            parameters = listOf("foo" to "xxx")
        ).body("it's a body")

        assertThat(request.httpString(), startsWith("POST http"))
        assertThat(request.httpString(), containsString("Content-Type"))
        assertThat(request.httpString(), containsString("body"))
    }

    @Test
    fun httpGetParameterArrayWillFormCorrectURL() {
        val lionel = "Lionel Ritchie"
        val list = arrayOf("once", "Twice", "Three", "Times", "Lady")
        val params = listOf("foo" to list, "bar" to lionel)

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/get"),
            response = mock.reflect()
        )

        val (response, error) = mock.path("get").httpGet(params).responseString().third
        val json = JSONObject(response)
        val query = json.getJSONObject("query")

        assertThat(error, nullValue())
        assertEquals(JSONArray("[\"$lionel\"]").toString(), query.getJSONArray("bar").toString())
        assertEquals(list.toList(), query.getJSONArray("foo[]").map { it.toString() })
    }
}
