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
 * Created by Kittinun Vantasin on 5/21/15.
 */

class RequestHeaderTest : BaseTestCase() {

    override fun setUp() {
        Manager.sharedInstance.basePath = "http://httpbin.org"
    }

    public fun testHttpPerRequestHeader() {

        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        val headerKey = "Custom"
        val headerValue = "foobar"

        Fuel.get("/get").header(headerKey to headerValue).response { req, res, either ->
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

        val statusCode = HttpURLConnection.HTTP_OK
        assertTrue(response?.httpStatusCode == statusCode, "http status code of valid request: $statusCode" )

        val string = String(data as ByteArray)
        assertTrue(string.contains(headerKey) && string.contains(headerValue), "header should be sent along with request header and present in response of httpbin.org")
    }

}