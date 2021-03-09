package fuel.serialization

import fuel.Fuel
import fuel.get
import kotlinx.serialization.Serializable
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test

internal class FuelKotlinxSerializationTest {

    @Serializable
    data class HttpBinUserAgentModel(var userAgent: String = "")

    @Test
    fun testSerializableResponse() {
        val mockWebServer = MockWebServer().apply {
            enqueue(MockResponse().setBody("{\"userAgent\": \"Fuel\"}"))
            start()
        }

        val response = Fuel.get(mockWebServer.url("user-agent")).execute()
        val json = response.toJson(deserialization = HttpBinUserAgentModel.serializer())
        assertEquals("Fuel", json.userAgent)

        mockWebServer.shutdown()
    }
}
