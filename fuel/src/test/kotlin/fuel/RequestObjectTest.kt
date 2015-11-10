package fuel

import fuel.core.*
import org.junit.Before
import org.junit.Test
import java.io.Reader
import java.util.concurrent.CountDownLatch
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Created by Kittinun Vantasin on 8/19/15.
 */

class RequestObjectTest : BaseTestCase() {

    init {
        Manager.instance.basePath = "http://httpbin.org"
    }

    //Model
    data class HttpBinUserAgentModel(var userAgent: String = "")

    //Deserializer
    class HttpBinUserAgentModelDeserializer : ResponseDeserializable<HttpBinUserAgentModel> {

        override fun deserialize(content: String): HttpBinUserAgentModel {
            return HttpBinUserAgentModel(content)
        }

    }

    class HttpBinMalformedDeserializer : ResponseDeserializable<HttpBinUserAgentModel> {

        override fun deserialize(reader: Reader): HttpBinUserAgentModel {
            throw IllegalStateException("Malformed data")
        }

    }

    @Before
    fun setUp() {
        lock = CountDownLatch(1)
    }

    @Test
    fun httpRequestObjectUserAgentValidTest() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        Fuel.get("user-agent").responseObject(HttpBinUserAgentModelDeserializer()) { req, res, either ->
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
        assertTrue(data is HttpBinUserAgentModel, "data should be HttpBinUserAgentModel type")
        assertTrue((data as HttpBinUserAgentModel).userAgent.isNotBlank(), "model must properly be serialized")
    }

    @Test
    fun httpRequestObjectUserAgentInvalidTest() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        Fuel.get("user-agent").responseObject(HttpBinMalformedDeserializer()) { req, res, either ->
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
        assertTrue(error?.exception is IllegalStateException, "exception is JSONException so that user can react with exception")
    }

}