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
 * Created by Kittinun Vantasin on 8/19/15.
 */

class RequestObjectTest : BaseTestCase() {

    init {
        Manager.instance.basePath = "http://httpbin.org"
        Manager.instance.callbackExecutor = object : Executor {
            override fun execute(command: Runnable) {
                command.run()
            }
        }
    }

    //Model
    data class HttpBinUserAgentModel(var userAgent: String = "")

    //Deserializer
    class HttpBinUserAgentModelDeserializer : ResponseDeserializable<HttpBinUserAgentModel> {

        override fun deserialize(content: String): HttpBinUserAgentModel? {
            return HttpBinUserAgentModel(content)
        }

    }

    Before
    fun setUp() {
        lock = CountDownLatch(1)
    }

    Test
    fun httpRequestObjectUserAgentTest() {
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

}