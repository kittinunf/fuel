package fuel.serialization

import fuel.Fuel
import fuel.get
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.fail

class FuelKotlinxSerializationTest {
    @Serializable
    data class HttpBinUserAgentModel(var userAgent: String = "")

    @Test
    fun testSerializableResponse() = runBlocking {
        val mockWebServer = MockWebServer().apply {
            enqueue(MockResponse().setBody("{\"userAgent\": \"Fuel\"}"))
            start()
        }

        val response = Fuel.get(mockWebServer.url("user-agent").toString())
        val json = response.toJson(Json.Default, HttpBinUserAgentModel.serializer())
        json.fold({
            assertEquals("Fuel", it?.userAgent)
        }, {
            fail(it.message)
        })

        mockWebServer.shutdown()
    }

    @Test
    fun testSerializableResponseWithDefaultJson() = runBlocking {
        val mockWebServer = MockWebServer().apply {
            enqueue(MockResponse().setBody("{\"userAgent\": \"Fuel2\"}"))
            start()
        }

        val response = Fuel.get(mockWebServer.url("user-agent").toString())
        val json = response.toJson(deserializationStrategy = HttpBinUserAgentModel.serializer())
        json.fold({
            assertEquals("Fuel2", it?.userAgent)
        }, {
            fail(it.message)
        })

        mockWebServer.shutdown()
    }
}
