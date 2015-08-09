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

class RequestAuthenticationTest : BaseTestCase() {

    val user: String
    val password: String

    init {
        user = "username"
        password = "password"
    }

    val manager: Manager by Delegates.lazy {
        build(Manager()) {
            client = HttpClient()
            basePath = "http://httpbin.org"
            callbackExecutor = object : Executor {
                override fun execute(command: Runnable) {
                    command.run()
                }
            }
        }
    }

    Before
    fun setUp() {
        lock = CountDownLatch(1)
    }

    Test
    fun httpBasicAuthenticationWithInvalidCase() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.request(Method.GET, "/basic-auth/$user/$password").authenticate("invalid", "authentication").response { req, res, either ->
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
        assertNotNull(error, "error should not be null")
        assertNull(data, "data should be null")

        val statusCode = HttpURLConnection.HTTP_UNAUTHORIZED
        assertTrue(response?.httpStatusCode == statusCode, "http status code of invalid credential should be $statusCode")
    }

    Test
    fun httpBasicAuthenticationWithValidCase() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.request(Method.GET, "/basic-auth/$user/$password").authenticate(user, password).response { req, res, either ->
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
        assertTrue(response?.httpStatusCode == statusCode, "http status code of valid credential should be $statusCode")
    }

}
