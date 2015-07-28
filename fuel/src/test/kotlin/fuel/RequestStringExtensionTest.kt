package fuel

import fuel.core.FuelError
import fuel.core.Manager
import fuel.core.Request
import fuel.core.Response
import org.junit.Test
import java.net.HttpURLConnection
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Created by Kittinun Vantasin on 7/28/15.
 */

public class RequestStringExtensionTest {

    init {
        Manager.sharedInstance.basePath = "https://httpbin.org"
        Manager.sharedInstance.additionalHeaders = mapOf("foo" to "bar")
        Manager.sharedInstance.additionalParams = mapOf("key" to "value")
    }

    Test
    fun httpGet() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        val countdown = CountDownLatch(1)

        "/get".get().responseString { req, res, either ->
            request = req
            response = res
            val (err, d) = either
            data = d
            error = err

            countdown.countDown()
        }

        countdown.await(30, TimeUnit.SECONDS)

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")

        val statusCode = HttpURLConnection.HTTP_OK
        assertTrue(response?.httpStatusCode == statusCode, "http status code of valid credential should be $statusCode" )
    }

    Test
    fun httpPost() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        val countdown = CountDownLatch(1)

        "/post".post().responseString { req, res, either ->
            request = req
            response = res
            val (err, d) = either
            data = d
            error = err

            countdown.countDown()
        }

        countdown.await(30, TimeUnit.SECONDS)

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")

        val statusCode = HttpURLConnection.HTTP_OK
        assertTrue(response?.httpStatusCode == statusCode, "http status code of valid credential should be $statusCode" )
    }

    Test
    fun httpPut() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        val countdown = CountDownLatch(1)

        "/put".put().responseString { req, res, either ->
            request = req
            response = res
            val (err, d) = either
            data = d
            error = err

            countdown.countDown()
        }

        countdown.await(30, TimeUnit.SECONDS)

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")

        val statusCode = HttpURLConnection.HTTP_OK
        assertTrue(response?.httpStatusCode == statusCode, "http status code of valid credential should be $statusCode" )
    }

    Test
    fun httpDelete() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        val countdown = CountDownLatch(1)

        "/delete".delete().responseString { req, res, either ->
            request = req
            response = res
            val (err, d) = either
            data = d
            error = err

            countdown.countDown()
        }

        countdown.await(30, TimeUnit.SECONDS)

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")

        val statusCode = HttpURLConnection.HTTP_OK
        assertTrue(response?.httpStatusCode == statusCode, "http status code of valid credential should be $statusCode" )
    }

}