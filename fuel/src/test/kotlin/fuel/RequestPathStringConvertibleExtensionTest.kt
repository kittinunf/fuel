package fuel

import fuel.core.*
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Created by Kittinun Vantasin on 8/10/15.
 */

class RequestPathStringConvertibleExtensionTest : BaseTestCase() {

    init {
        Manager.instance.basePath = "https://httpbin.org"
        Manager.instance.callbackExecutor = object : Executor {
            override fun execute(command: Runnable) {
                command.run()
            }
        }
    }

    enum class HttpsBin(val relativePath: String) : Fuel.PathStringConvertible {
        COOKIES("cookies"),
        POST("post"),
        PUT("put"),
        DELETE("delete");

        override val path = "/$relativePath"
    }

    Before
    fun setUp() {
        lock = CountDownLatch(1)
    }

    Test
    fun httpGetRequestWithSharedInstance() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        HttpsBin.COOKIES.httpGet().responseString { req, res, either ->
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

    Test
    fun httpPostRequestWithSharedInstance() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        HttpsBin.POST.httpPost().responseString { req, res, either ->
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

    Test
    fun httpPutRequestWithSharedInstance() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        HttpsBin.PUT.httpPut().responseString { req, res, either ->
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

    Test
    fun httpDeleteRequestWithSharedInstance() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        HttpsBin.DELETE.httpDelete().responseString { req, res, either ->
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

}