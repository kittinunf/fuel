package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.*
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection
import java.util.concurrent.CountDownLatch
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Created by Kittinun Vantasin on 5/22/15.
 */

class RequestTest : BaseTestCase() {

    val manager: Manager by lazy { Manager() }

    enum class HttpsBin(val relativePath: String) : Fuel.PathStringConvertible {
        USER_AGENT("user-agent"),
        POST("post"),
        PUT("put"),
        DELETE("delete");

        override val path = "https://httpbin.org/$relativePath"
    }

    @Before
    fun setUp() {
        lock = CountDownLatch(1)
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

            lock.countDown()
        }

        await()

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")
        assertTrue(data is ByteArray, "data should be ByteArray type")
        assertTrue(response?.httpStatusCode == HttpURLConnection.HTTP_OK, "http status code should be ${HttpURLConnection.HTTP_OK}")
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

            lock.countDown()
        }

        await()

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")
        assertTrue(data is String, "data should be String type")
        assertTrue(response?.httpStatusCode == HttpURLConnection.HTTP_OK, "http status code should be ${HttpURLConnection.HTTP_OK}")
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

            lock.countDown()
        }

        await()

        val string = data as String

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")
        assertTrue(response?.httpStatusCode == HttpURLConnection.HTTP_OK, "http status code should be ${HttpURLConnection.HTTP_OK}")

        assertTrue(string.contains(paramKey) && string.contains(paramValue), "url query param should be sent along with url and present in response of httpbin.org")
    }

    @Test
    fun httpPostRequestWithParameters() {
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

            lock.countDown()
        }

        await()

        val string = data as String

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")
        assertTrue(response?.httpStatusCode == HttpURLConnection.HTTP_OK, "http status code should be ${HttpURLConnection.HTTP_OK}")

        assertTrue(string.contains(paramKey) && string.contains(paramValue), "url query param should be sent along with url and present in response of httpbin.org")
    }

    @Test
    fun httpPostRequestWithBody() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        val body = "{ \"foo\" : \"bar\" }"

        manager.request(Method.POST, "http://httpbin.org/post").body(body).responseString { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            data = d
            error = err

            lock.countDown()
        }

        await()

        val string = data as String

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")
        assertTrue(response?.httpStatusCode == HttpURLConnection.HTTP_OK, "http status code should be ${HttpURLConnection.HTTP_OK}")

        assertTrue(string.contains("foo") && string.contains("bar"), "body should be sent along with url and present in response of httpbin.org")
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

            lock.countDown()
        }

        await()

        val string = data as String

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")
        assertTrue(response?.httpStatusCode == HttpURLConnection.HTTP_OK, "http status code should be ${HttpURLConnection.HTTP_OK}")

        assertTrue(string.contains(paramKey) && string.contains(paramValue), "url query param should be sent along with url and present in response of httpbin.org")
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

            lock.countDown()
        }

        await()

        val string = data as String

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")
        assertTrue(response?.httpStatusCode == HttpURLConnection.HTTP_OK, "http status code should be ${HttpURLConnection.HTTP_OK}")

        assertTrue(string.contains(paramKey) && string.contains(paramValue), "url query param should be sent along with url and present in response of httpbin.org")
    }

    @Test
    fun httpGetRequestWithPathStringConvertible() {
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

            lock.countDown()
        }

        await()

        val string = data as String

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")
        assertTrue(response?.httpStatusCode == HttpURLConnection.HTTP_OK, "http status code should be ${HttpURLConnection.HTTP_OK}")
        assertTrue(string.contains("user-agent"), "USER_AGENT endpoint must be resolved correctly, and user-agent should be present in this response")
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

            lock.countDown()
        }

        await()

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")
        assertTrue(response?.httpStatusCode == HttpURLConnection.HTTP_OK, "http status code should be ${HttpURLConnection.HTTP_OK}")
    }

    @Test
    fun httpGetRequestWithRequestConvertibleAndOverriddenParameters() {
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

            lock.countDown()
        }

        await()

        val string = data as String

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")
        assertTrue(response?.httpStatusCode == HttpURLConnection.HTTP_OK, "http status code should be ${HttpURLConnection.HTTP_OK}")

        assertTrue(string.contains(paramKey), "url query param should be sent along with url, $paramKey")
        assertTrue(string.contains(paramValue), "url query param should be sent along with url, $paramValue")
    }

    @Test
    fun httpGetRequestCancel() {
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        val request = manager.request(Method.GET, "http://httpbin.org/stream-bytes/4194304").responseString { req, res, result ->
            response = res

            val (d, err) = result
            data = d
            error = err
        }

        request.cancel()

        println(request.cUrlString())
        assertNotNull(request, "request should not be null")
        assertNull(response, "response should be null")
        assertNull(data, "data should be null")
        assertNull(error, "error should be null")
    }

    @Test
    fun httpGetSyncRequest() {
        // Given
        var expectedHost = "httpbin.org"
        var received:Received? = null

        // When
        val request = manager.request(Method.GET, "http://${expectedHost}/get").sync().responseString { req, res, result ->
            received = Received(res, result.value, result.error)
        }

        // Then
        assertThat(received, notNullValue())
        assertThat(received?.response, notNullValue())
        assertThat(received?.response?.httpStatusCode, `is`(200))
        assertThat(received?.data as String, containsString(expectedHost))
        assertThat(received?.error, nullValue())
    }
}

data class Received(val response:Response? = null, val data: Any? = null, val error:FuelError? = null)