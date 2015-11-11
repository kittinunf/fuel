package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.net.HttpURLConnection
import java.util.concurrent.CountDownLatch
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Created by Kittinun Vantasin on 5/22/15.
 */

class RequestSharedInstanceTest : BaseTestCase() {

    init {
        Manager.instance.basePath = "https://httpbin.org"
        Manager.instance.baseHeaders = mapOf("foo" to "bar")
        Manager.instance.baseParams = listOf("key" to "value")
    }

    enum class HttpsBin(override val path: String) : Fuel.PathStringConvertible {
        IP("ip"),
        POST("post"),
        PUT("put"),
        DELETE("delete")
    }

    class HttpBinConvertible(val method: Method, val relativePath: String) : Fuel.RequestConvertible {
        override val request = createRequest()

        fun createRequest(): Request {
            val encoder = Encoding().apply {
                httpMethod = method
                urlString = "https://httpbin.org$relativePath"
                parameters = listOf("foo" to "bar")
            }
            return encoder.request
        }
    }

    @Before
    fun setUp() {
        lock = CountDownLatch(1)
    }

    @Test
    fun httpGetRequestWithSharedInstance() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        Fuel.get("/get").responseString { req, res, either ->
            request = req
            response = res

            val (err, d) = either
            data = d
            error = err

            lock.countDown()
        }

        await()

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")
        assertTrue(response?.httpStatusCode == HttpURLConnection.HTTP_OK, "http status code should be ${HttpURLConnection.HTTP_OK}")

        val string = data as String
        assertTrue(string.toLowerCase().contains("foo") && string.toLowerCase().contains("bar"), "additional header must be sent along with request and present in response")
        assertTrue(string.toLowerCase().contains("key") && string.toLowerCase().contains("value"), "additional param must be sent along with request and present in response")
        assertTrue(string.contains("https"), "url should contain https to indicate usage of shared instance")
    }

    @Test
    fun httpPostRequestWithSharedInstance() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        Fuel.post("/post").responseString { req, res, either ->
            request = req
            response = res

            val (err, d) = either
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

        assertTrue(string.toLowerCase().contains("foo") && string.toLowerCase().contains("bar"), "additional header must be sent along with request and present in response")
        assertTrue(string.toLowerCase().contains("key") && string.toLowerCase().contains("value"), "additional param must be sent along with request and present in response")
        assertTrue(string.contains("https"), "url should contain https to indicate usage of shared instance")
    }

    @Test
    fun httpPutRequestWithSharedInstance() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        Fuel.put("/put").responseString { req, res, either ->
            request = req
            response = res

            val (err, d) = either
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

        assertTrue(string.toLowerCase().contains("foo") && string.toLowerCase().contains("bar"), "additional header must be sent along with request and present in response")
        assertTrue(string.toLowerCase().contains("key") && string.toLowerCase().contains("value"), "additional param must be sent along with request and present in response")
        assertTrue(string.contains("https"), "url should contain https to indicate usage of shared instance")
    }

    @Test
    fun httpDeleteRequestWithSharedInstance() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        Fuel.delete("/delete").responseString { req, res, either ->
            request = req
            response = res

            val (err, d) = either
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

        assertTrue(string.toLowerCase().contains("foo") && string.toLowerCase().contains("bar"), "additional header must be sent along with request and present in response")
        assertTrue(string.toLowerCase().contains("key") && string.toLowerCase().contains("value"), "additional param must be sent along with request and present in response")
        assertTrue(string.contains("https"), "url should contain https to indicate usage of shared instance")
    }

    @Test
    fun httpGetRequestWithPathStringConvertibleAndSharedInstance() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        Fuel.get(HttpsBin.IP).responseString { req, res, either ->
            request = req
            response = res

            val (err, d) = either
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

        assertTrue(string.contains("origin"), "response should contain \"origin\" ")
    }

    @Test
    fun httpPostRequestWithPathStringConvertibleAndSharedInstance() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        Fuel.post(HttpsBin.POST).responseString { req, res, either ->
            request = req
            response = res

            val (err, d) = either
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

        assertTrue(string.contains("https"), "url should contain https to indicate usage of shared instance")
    }

    @Test
    fun httpPutRequestWithPathStringConvertibleAndSharedInstance() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        Fuel.put(HttpsBin.PUT).responseString { req, res, either ->
            request = req
            response = res

            val (err, d) = either
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

        assertTrue(string.contains("https"), "url should contain https to indicate usage of shared instance")
    }

    @Test
    fun httpDeleteRequestWithPathStringConvertibleAndSharedInstance() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        Fuel.delete(HttpsBin.DELETE).responseString { req, res, either ->
            request = req
            response = res

            val (err, d) = either
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

        assertTrue(string.contains("https"), "url should contain https to indicate usage of shared instance")
    }

    @Test
    fun httpGetRequestWithRequestConvertibleAndSharedInstance() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        Fuel.request(HttpBinConvertible(Method.GET, "/get")).responseString { req, res, either ->
            request = req
            response = res

            val (err, d) = either
            data = d
            error = err

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
    fun httpPostRequestWithRequestConvertibleAndSharedInstance() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        Fuel.request(HttpBinConvertible(Method.POST, "/post")).responseString { req, res, either ->
            request = req
            response = res

            val (err, d) = either
            data = d
            error = err

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
    fun httpPutRequestWithRequestConvertibleAndSharedInstance() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        Fuel.request(HttpBinConvertible(Method.PUT, "/put")).responseString { req, res, either ->
            request = req
            response = res

            val (err, d) = either
            data = d
            error = err

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
    fun httpDeleteRequestWithRequestConvertibleAndSharedInstance() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        Fuel.request(HttpBinConvertible(Method.DELETE, "/delete")).responseString { req, res, either ->
            request = req
            response = res

            val (err, d) = either
            data = d
            error = err

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
    fun httpUploadWithProgressValidCase() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        var read = -1L
        var total = -1L

        Fuel.upload("/post").source { request, url ->
            val dir = System.getProperty("user.dir")
            File(dir, "src/test/assets/lorem_ipsum_long.tmp")
        }.progress { readBytes, totalBytes ->
            read = readBytes
            total = totalBytes
            println("read: $read, total: $total")
        }.responseString { req, res, either ->
            request = req
            response = res
            val (err, d) = either
            data = d
            error = err

            lock.countDown()
        }

        await()

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")

        assertTrue(read == total && read != -1L && total != -1L, "read bytes and total bytes should be equal")
        val statusCode = HttpURLConnection.HTTP_OK
        assertTrue(response?.httpStatusCode == statusCode, "http status code should be $statusCode" )
    }


    @Test
    fun httpDownloadWithProgressValidCase() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        var read = -1L
        var total = -1L

        val numberOfBytes = 1048576
        Fuel.download("/bytes/$numberOfBytes").destination { response, url ->
            File.createTempFile(numberOfBytes.toString(), null)
        }.progress { readBytes, totalBytes ->
            read = readBytes
            total = totalBytes
        }.responseString { req, res, either ->
            request = req
            response = res
            val (err, d) = either
            data = d
            error = err

            lock.countDown()
        }

        await()

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")

        assertTrue(read == total, "read bytes and total bytes should be equal")
        assertTrue(response?.httpStatusCode == HttpURLConnection.HTTP_OK, "http status code should be ${HttpURLConnection.HTTP_OK}")
    }

}
