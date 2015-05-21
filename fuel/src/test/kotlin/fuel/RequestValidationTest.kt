package fuel

import fuel.core.FuelError
import fuel.core.Manager
import fuel.core.Request
import fuel.core.Response
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Created by Kittinun Vantasin on 5/21/15.
 */

class RequestValidationTest : BaseTestCase() {

    override fun setUp() {
        Manager.sharedInstance.basePath = "http://httpbin.org"
    }

    public fun testHttpValidationWithDefaultCase() {

        val preDefineStatusCode = 418

        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        //this validate (200..299)
        Fuel.get("/status/$preDefineStatusCode").response { req, res, either ->
            request = req
            response = res

            val (err, d) = either
            data = d
            error = err

            expectFulfill()
        }

        expectWait()

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNotNull(error, "error should not be null")
        assertNull(data, "data should be null")
        assertTrue(response?.httpStatusCode == preDefineStatusCode, "http status code should be $preDefineStatusCode" )
    }

    public fun testHttpValidationWithCustomCase() {

        val preDefineStatusCode = 418

        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        Fuel.get("/status/$preDefineStatusCode").validate(400..419).response { req, res, either ->
            request = req
            response = res

            val (err, d) = either
            data = d
            error = err

            expectFulfill()
        }

        expectWait()

        assertNotNull(request, "request should not be null")
        assertNotNull(response, "response should not be null")
        assertNull(error, "error should be null")
        assertNotNull(data, "data should not be null")
        assertTrue(response?.httpStatusCode == preDefineStatusCode, "http status code should be $preDefineStatusCode" )
    }

}
