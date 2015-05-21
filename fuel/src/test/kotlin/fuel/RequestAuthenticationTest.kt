package fuel

import fuel.core.FuelError
import fuel.core.Manager
import fuel.core.Request
import fuel.core.Response
import java.net.HttpURLConnection
import kotlin.properties.Delegates
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Created by Kittinun Vantasin on 5/21/15.
 */

class RequestAuthenticationTest : BaseTestCase() {

    var user: String by Delegates.notNull()
    var password: String by Delegates.notNull()

    override fun setUp() {
        user = "user"
        password = "password"

        Manager.sharedInstance.basePath = "http://httpbin.org"
    }

    public fun testHttpBasicAuthenticationWithInvalidCase() {

        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        Fuel.get("/basic-auth/$user/$password").authenticate("invalid", "authentication").response { req, res, either ->
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

        val statusCode = HttpURLConnection.HTTP_UNAUTHORIZED
        assertTrue(response?.httpStatusCode == statusCode, "http status code of invalid credential should be $statusCode" )
    }

    public fun testHttpBasicAuthenticationWithValidCase() {

        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        Fuel.get("/basic-auth/$user/$password").authenticate(user, password).response { req, res, either ->
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
        assertTrue(response?.httpStatusCode == statusCode, "http status code of valid credential should be $statusCode" )
    }

}
