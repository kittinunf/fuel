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
        assertEquals("Fuel", json.userAgent)

        mockWebServer.shutdown()
    }
}
