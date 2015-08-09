package fuel

import fuel.core.FuelError
import fuel.core.Manager
import fuel.core.Request
import fuel.core.Response
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Created by Kittinun Vantasin on 7/28/15.
 */

class RequestStringExtensionTest : BaseTestCase() {

    init {
        Manager.instance.basePath = "https://httpbin.org"
        Manager.instance.baseHeaders = mapOf("foo" to "bar")
        Manager.instance.baseParams = mapOf("key" to "value")

        Manager.instance.callbackExecutor = object : Executor {
            override fun execute(command: Runnable) {
                command.run()
            }
        }
    }

    Before
    fun setUp() {
        lock = CountDownLatch(1)
    }

    Test
    fun httpGet() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        "/get".httpGet().responseString { req, res, either ->
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

        val statusCode = HttpURLConnection.HTTP_OK
        assertTrue(response?.httpStatusCode == statusCode, "http status code should be $statusCode" )
    }

    Test
    fun httpPost() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        "/post".httpPost().responseString { req, res, either ->
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

        val statusCode = HttpURLConnection.HTTP_OK
        assertTrue(response?.httpStatusCode == statusCode, "http status code should be $statusCode" )
    }

    Test
    fun httpPut() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        "/put".httpPut().responseString { req, res, either ->
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

        val statusCode = HttpURLConnection.HTTP_OK
        assertTrue(response?.httpStatusCode == statusCode, "http status code should be $statusCode" )
    }

    Test
    fun httpDelete() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        "/delete".httpDelete().responseString { req, res, either ->
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

        val statusCode = HttpURLConnection.HTTP_OK
        assertTrue(response?.httpStatusCode == statusCode, "http status code should be $statusCode" )
    }

}