package fuel

import fuel.core.*
import fuel.toolbox.HttpClient
import fuel.util.build
import kotlin.properties.Delegates
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Created by Kittinun Vantasin on 5/21/15.
 */

class RequestValidationTest : BaseTestCase() {

    override val numberOfTestCase = 2

    val manager: Manager by Delegates.lazy {
        build(Manager()) {
            client = HttpClient()
            basePath = "http://httpbin.org"
        }
    }

    public fun testHttpValidationWithDefaultCase() {
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

            countdownFulfill()
        }

        countdownWait()

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNotNull(error, "error should not be null")
        assertNotNull(error?.errorDataStream, "error data stream should not be null")
        assertNotNull(error?.errorData, "error data should not be null")
        assertNull(data, "data should be null")
        assertTrue(response?.httpStatusCode == preDefinedStatusCode, "http status code should be $preDefinedStatusCode" )
    }

    public fun testHttpValidationWithCustomCase() {
        val preDefineStatusCode = 418

        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.request(Method.GET, "/status/$preDefineStatusCode").validate(400..419).response { req, res, either ->
            request = req
            response = res

            val (err, d) = either
            data = d
            error = err

            countdownFulfill()
        }

        countdownWait()

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")
        assertTrue(response?.httpStatusCode == preDefineStatusCode, "http status code should be $preDefineStatusCode" )
    }

}
