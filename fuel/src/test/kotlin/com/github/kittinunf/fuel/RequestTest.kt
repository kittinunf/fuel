package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.*
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Test
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RequestTest : BaseTestCase() {
    private val manager: FuelManager by lazy { FuelManager() }

    enum class HttpsBin(relativePath: String) : Fuel.PathStringConvertible {
        USER_AGENT("user-agent"),
        POST("post"),
        PUT("put"),
        PATCH("patch"),
        DELETE("delete");

        override val path = "https://httpbin.org/$relativePath"
    }

    enum class MockBin(path: String) : Fuel.PathStringConvertible {
        PATH("");

        override val path = "http://mockbin.org/request/$path"
    }

    class HttpBinConvertible(val method: Method, private val relativePath: String) : Fuel.RequestConvertible {
        override val request = createRequest()

        private fun createRequest(): Request {
            val encoder = Encoding(
                    httpMethod = method,
                    urlString = "http://httpbin.org/$relativePath",
                    parameters = listOf("foo" to "bar")
            )
            return encoder.request
        }
    }

    init {
        val acceptsAllTrustManager = object : X509TrustManager {
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}

            override fun getAcceptedIssuers(): Array<X509Certificate>? = null

            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        }

        manager.socketFactory = {
            val context = SSLContext.getInstance("TLS")
            context.init(null, arrayOf<TrustManager>(acceptsAllTrustManager), SecureRandom())
            SSLContext.setDefault(context)
            context.socketFactory
        }()
    }

    @Test
    fun testResponseURLShouldSameWithRequestURL() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.request(Method.GET, "http://httpbin.org/get").response { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            data = d
            error = err
        }

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(request?.url, notNullValue())
        assertThat(response?.url, notNullValue())
        assertThat(request?.url, isEqualTo(response?.url))
    }

    @Test
    fun httpGetRequestWithDataResponse() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.request(Method.GET, "http://httpbin.org/get").response { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            data = d
            error = err
        }

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpGetRequestWithStringResponse() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.request(Method.GET, "http://httpbin.org/get").responseString { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            data = d
            error = err
        }

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpGetRequestWithImageResponse() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.request(Method.GET, "http://httpbin.org/image/png").responseString { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            data = d
            error = err
        }

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response?.toString(), containsString("bytes of image/png"))

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpGetRequestWithBytesResponse() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.request(Method.GET, "http://httpbin.org/bytes/555").responseString { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            data = d
            error = err
        }

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response?.toString(), containsString("Body : (555 bytes of application/octet-stream)"))

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun testProcessBodyWithUnknownContentTypeAndNoData() {
        var request: Request? = null
        var response: Response? = null
        var error: FuelError? = null

        manager.request(Method.GET, "http://httpbin.org/bytes/555").responseString { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            error = err
        }

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())

        assertThat(response?.processBody("(unknown)", ByteArray(0)), isEqualTo("(empty)"))
    }

    @Test
    fun testGuessContentTypeWithNoContentTypeInHeaders() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.request(Method.GET, "http://httpbin.org/bytes/555").responseString { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            data = d
            error = err
        }

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val headers: Map<String, List<String>> = mapOf(Pair("Content-Type", listOf("")))
        assertThat(response?.guessContentType(headers), isEqualTo("(unknown)"))
    }

    @Test
    fun testGuessContentTypeWithNoHeaders() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.request(Method.GET, "http://httpbin.org/image/png").responseString { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            data = d
            error = err
        }

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val headers: Map<String, List<String>> = mapOf(Pair("Content-Type", listOf("")))
        assertThat(response?.guessContentType(headers), isEqualTo("image/png"))
    }

    @Test
    fun httpGetRequestWithParameters() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        val paramKey = "foo"
        val paramValue = "bar"

        manager.request(Method.GET, "http://httpbin.org/get", listOf(paramKey to paramValue)).responseString { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            data = d
            error = err
        }

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))

        assertThat(string, containsString(paramKey))
        assertThat(string, containsString(paramValue))
    }

    @Test
    fun httpPostRequestWithParameters() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        val paramKey = "foo"
        val paramValue = "bar"

        manager.request(Method.POST, "http://httpbin.org/post", listOf(paramKey to paramValue)).responseString { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            data = d
            error = err
        }

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))

        assertThat(string, containsString(paramKey))
        assertThat(string, containsString(paramValue))
    }

    @Test
    fun httpPostRequestWithBody() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        val foo = "foo"
        val bar = "bar"
        val body = "{ $foo : $bar }"

        manager.request(Method.POST, "http://httpbin.org/post").body(body).responseString { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            data = d
            error = err
        }

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))

        assertThat(string, containsString(foo))
        assertThat(string, containsString(bar))
    }

    @Test
    fun httpPutRequestWithParameters() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        val paramKey = "foo"
        val paramValue = "bar"

        manager.request(Method.PUT, "http://httpbin.org/put", listOf(paramKey to paramValue)).responseString { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            data = d
            error = err
        }

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))

        assertThat(string, containsString(paramKey))
        assertThat(string, containsString(paramValue))
    }

    @Test
    fun httpPatchRequestWithParameters() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        val paramKey = "foo2"
        val paramValue = "bar2"

        // for some reason httpbin doesn't support underlying POST for PATCH endpoint
        manager.request(Method.PATCH, "http://mockbin.org/request", listOf(paramKey to paramValue)).responseString { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            data = d
            error = err
        }

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))

        assertThat(string, containsString(paramKey))
        assertThat(string, containsString(paramValue))
    }

    @Test
    fun httpDeleteRequestWithParameters() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        val paramKey = "foo"
        val paramValue = "bar"

        manager.request(Method.DELETE, "http://httpbin.org/delete", listOf(paramKey to paramValue)).responseString { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            data = d
            error = err
        }

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))

        assertThat(string, containsString(paramKey))
        assertThat(string, containsString(paramValue))
    }

    @Test
    fun httpHeadRequest() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        val paramKey = "foo"
        val paramValue = "bar"

        manager.request(Method.HEAD, "http://httpbin.org/get", listOf(paramKey to paramValue)).responseString { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            data = d
            error = err
        }

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))

        assertThat(string, equalTo(""))
    }

    @Test
    fun httpHeadGzipRequest() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        val paramKey = "foo"
        val paramValue = "bar"

        manager.request(Method.HEAD, "http://httpbin.org/gzip", listOf(paramKey to paramValue)).responseString { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            data = d
            error = err
        }

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))

        assertThat(string, equalTo(""))
    }

    @Test
    fun httpGetRequestUserAgentWithPathStringConvertible() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.request(Method.GET, HttpsBin.USER_AGENT).responseString { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            data = d
            error = err
        }

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))

        assertThat(string, containsString("user-agent"))
    }

    @Test
    fun httpGetRequestWithRequestConvertible() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.request(HttpBinConvertible(Method.GET, "get")).responseString { req, res, result ->
            request = req
            response = res

            result.fold({
                data = it
            }, {
                error = it
            })
        }

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpPatchRequestWithRequestConvertible() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        val paramKey = "foo"
        val paramValue = "bar"

        manager.request(Method.PATCH, MockBin.PATH, listOf(paramKey to paramValue)).responseString { req, res, result ->
            request = req
            response = res

            result.fold({
                data = it
            }, {
                error = it
            })
        }

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpPostRequestWithRequestConvertibleAndOverriddenParameters() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        val paramKey = "foo"
        val paramValue = "xxx"

        manager.request(Method.POST, "http://httpbin.org/post", listOf(paramKey to paramValue)).responseString { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            data = d
            error = err
        }
        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))

        assertThat(string, containsString(paramKey))
        assertThat(string, containsString(paramValue))
    }

    @Test
    fun httpGetRequestCancel() {
        regularMode {
            var response: Response? = null
            var data: Any? = null
            var error: FuelError? = null

            val request = manager.request(Method.GET, "http://httpbin.org/stream-bytes/4194304").responseString { _, res, result ->
                response = res

                val (d, err) = result
                data = d
                error = err
            }

            request.cancel()

            assertThat(request, notNullValue())
            assertThat(response, nullValue())
            assertThat(data, nullValue())
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

}

