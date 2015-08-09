package fuel

import fuel.core.*
import fuel.toolbox.HttpClient
import fuel.util.build
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import kotlin.properties.Delegates
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Created by Kittinun Vantasin on 5/21/15.
 */

class RequestHeaderTest : BaseTestCase() {

    val manager: Manager by Delegates.lazy {
        Manager.callbackExecutor = object : Executor {
            override fun execute(command: Runnable) {
                command.run()
            }
        }

        build(Manager()) {
            client = HttpClient()
            basePath = "http://httpbin.org"
        }
    }

    Before
    fun setUp() {
        lock = CountDownLatch(1)
    }

    Test
    fun httpRequestHeader() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        val headerKey = "Custom"
        val headerValue = "foobar"

        manager.request(Method.GET, "/get").header(headerKey to headerValue).response { req, res, either ->
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
        assertTrue(response?.httpStatusCode == statusCode, "http status code of valid request: $statusCode" )

        val string = String(data as ByteArray)
        assertTrue(string.contains(headerKey) && string.contains(headerValue), "header should be sent along with request header and present in response of httpbin.org")
    }

}