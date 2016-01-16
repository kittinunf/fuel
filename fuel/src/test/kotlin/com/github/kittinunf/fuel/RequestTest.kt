package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.*
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection
import java.util.concurrent.CountDownLatch
import org.hamcrest.CoreMatchers.`is` as isEqualTo

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

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.httpStatusCode, isEqualTo(statusCode))
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

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.httpStatusCode, isEqualTo(statusCode))
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

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.httpStatusCode, isEqualTo(statusCode))

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

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.httpStatusCode, isEqualTo(statusCode))

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

            lock.countDown()
        }

        await()

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.httpStatusCode, isEqualTo(statusCode))

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

            lock.countDown()
        }

        await()

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.httpStatusCode, isEqualTo(statusCode))

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

            lock.countDown()
        }

        await()

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.httpStatusCode, isEqualTo(statusCode))

        assertThat(string, containsString(paramKey))
        assertThat(string, containsString(paramValue))
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

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.httpStatusCode, isEqualTo(statusCode))

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

            lock.countDown()
        }

        await()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.httpStatusCode, isEqualTo(statusCode))
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

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.httpStatusCode, isEqualTo(statusCode))

        assertThat(string, containsString(paramKey))
        assertThat(string, containsString(paramValue))
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
        assertThat(request, notNullValue())
        assertThat(response, nullValue())
        assertThat(data, nullValue())
        assertThat(error, nullValue())
    }

    @Test
    fun httpGetSyncRequest() {
        var received: Received = Received()
        manager.request(Method.GET, "http://httpbin.org/get").sync().responseString { req, res, result ->
            received = Received(res, result.value, result.error)
        }

        assertThat(received, notNullValue())
        assertThat(received.response, notNullValue())
        assertThat(received.response?.httpStatusCode, isEqualTo(200))
        assertThat(received.data as String, containsString("httpbin.org"))
        assertThat(received.error, nullValue())
    }

    data class Received(val response: Response? = null, val data: Any? = null, val error: FuelError? = null)
}

