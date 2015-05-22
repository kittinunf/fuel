package fuel

import fuel.core.FuelError
import fuel.core.Manager
import fuel.core.Request
import fuel.core.Response
import java.net.HttpURLConnection
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Created by Kittinun Vantasin on 5/22/15.
 */

class RequestSharedInstanceTest : BaseTestCase() {

    override val numberOfTestCase = 8

    enum class HttpsBin(override val path: String) : Fuel.PathStringConvertible {
        IP : HttpsBin("ip")
        POST : HttpsBin("post")
        PUT : HttpsBin("put")
        DELETE : HttpsBin("delete")
    }

    override fun setUp() {
        Manager.sharedInstance.basePath = "https://httpbin.org"
        Manager.sharedInstance.additionalHeaders = mapOf("foo" to "bar")
    }

    public fun testHttpGetRequestWithSharedInstance() {
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

            countdownFulfill()
        }

        countdownWait()

        val string = data as String

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")
        assertTrue(response?.httpStatusCode == HttpURLConnection.HTTP_OK, "http status code should be ${HttpURLConnection.HTTP_OK}")

        assertTrue(string.toLowerCase().contains("foo") && string.toLowerCase().contains("bar"), "additional header must be sent along with request and present in response")
        assertTrue(string.contains("https"), "url should contain https to indicate usage of shared instance")
    }

    public fun testHttpPostRequestWithSharedInstance() {
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

            countdownFulfill()
        }

        countdownWait()

        val string = data as String

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")
        assertTrue(response?.httpStatusCode == HttpURLConnection.HTTP_OK, "http status code should be ${HttpURLConnection.HTTP_OK}")

        assertTrue(string.toLowerCase().contains("foo") && string.toLowerCase().contains("bar"), "additional header must be sent along with request and present in response")
        assertTrue(string.contains("https"), "url should contain https to indicate usage of shared instance")
    }

    public fun testHttpPutRequestWithSharedInstance() {
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

            countdownFulfill()
        }

        countdownWait()

        val string = data as String

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")
        assertTrue(response?.httpStatusCode == HttpURLConnection.HTTP_OK, "http status code should be ${HttpURLConnection.HTTP_OK}")

        assertTrue(string.toLowerCase().contains("foo") && string.toLowerCase().contains("bar"), "additional header must be sent along with request and present in response")
        assertTrue(string.contains("https"), "url should contain https to indicate usage of shared instance")
    }

    public fun testHttpDeleteRequestWithSharedInstance() {
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

            countdownFulfill()
        }

        countdownWait()

        val string = data as String

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")
        assertTrue(response?.httpStatusCode == HttpURLConnection.HTTP_OK, "http status code should be ${HttpURLConnection.HTTP_OK}")

        assertTrue(string.toLowerCase().contains("foo") && string.toLowerCase().contains("bar"), "additional header must be sent along with request and present in response")
        assertTrue(string.contains("https"), "url should contain https to indicate usage of shared instance")
    }

    public fun testHttpGetRequestWithPathStringConvertibleAndSharedInstance() {
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

            countdownFulfill()
        }

        countdownWait()

        val string = data as String

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")
        assertTrue(response?.httpStatusCode == HttpURLConnection.HTTP_OK, "http status code should be ${HttpURLConnection.HTTP_OK}")

        assertTrue(string.contains("origin"), "response should contain \"origin\" ")
    }

    public fun testHttpPostRequestWithPathStringConvertibleAndSharedInstance() {
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

            countdownFulfill()
        }

        countdownWait()

        val string = data as String

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")
        assertTrue(response?.httpStatusCode == HttpURLConnection.HTTP_OK, "http status code should be ${HttpURLConnection.HTTP_OK}")

        assertTrue(string.contains("https"), "url should contain https to indicate usage of shared instance")
    }

    public fun testHttpPutRequestWithPathStringConvertibleAndSharedInstance() {
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

            countdownFulfill()
        }

        countdownWait()

        val string = data as String

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")
        assertTrue(response?.httpStatusCode == HttpURLConnection.HTTP_OK, "http status code should be ${HttpURLConnection.HTTP_OK}")

        assertTrue(string.contains("https"), "url should contain https to indicate usage of shared instance")
    }

    public fun testHttpDeleteRequestWithPathStringConvertibleAndSharedInstance() {
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

            countdownFulfill()
        }

        countdownWait()

        val string = data as String

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")
        assertTrue(response?.httpStatusCode == HttpURLConnection.HTTP_OK, "http status code should be ${HttpURLConnection.HTTP_OK}")

        assertTrue(string.contains("https"), "url should contain https to indicate usage of shared instance")
    }

}
