package fuel

import fuel.core.*
import fuel.toolbox.HttpClient
import fuel.util.build
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import kotlin.properties.Delegates
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Created by Kittinun Vantasin on 5/21/15.
 */

class RequestValidationTest : BaseTestCase() {

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
    fun httpValidationWithDefaultCase() {
        val preDefinedStatusCode = 418

        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        //this validate (200..299) which should fail with 418
        manager.request(Method.GET, "/status/$preDefinedStatusCode").response { req, res, either ->
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
        assertNotNull(error?.errorData, "error data should not be null")
        assertNull(data, "data should be null")
        assertTrue(response?.httpStatusCode == preDefinedStatusCode, "http status code should be $preDefinedStatusCode" )
    }

    Test
    fun httpValidationWithCustomValidCase() {
        val preDefinedStatusCode = 203

        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        //this validate (200..299) which should fail with 418
        manager.request(Method.GET, "/status/$preDefinedStatusCode").validate(200..202).responseString { req, res, either ->
            request = req
            response = res

            val (d, err) = either.swap()
            data = d
            error = err

            lock.countDown()
        }

        await()

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNotNull(error, "error should not be null")
        assertNull(data, "data should be null")
        assertTrue(response?.httpStatusCode == preDefinedStatusCode, "http status code should be $preDefinedStatusCode" )
    }

    Test
    fun httpValidationWithCustomInvalidCase() {
        val preDefineStatusCode = 418

        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.request(Method.GET, "/status/$preDefineStatusCode").validate(400..419).response { req, res, either ->
            request = req
            response = res

            when (either) {
                is Left -> {
                    error = either.get()
                }
                is Right -> {
                    data = either.get()
                }
            }

            lock.countDown()
        }

        await()

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")
        assertTrue(response?.httpStatusCode == preDefineStatusCode, "http status code should be $preDefineStatusCode" )
    }

}
