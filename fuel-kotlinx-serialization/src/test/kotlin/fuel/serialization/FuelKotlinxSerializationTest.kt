package fuel.serialization

import fuel.HttpLoader
import fuel.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test

class FuelKotlinxSerializationTest {
    @Serializable
    data class HttpBinUserAgentModel(var userAgent: String = "")

    @Test
    fun testSerializableResponse() = runBlocking {
        val mockWebServer = MockWebServer().apply {
            enqueue(MockResponse().setBody("{\"userAgent\": \"Fuel\"}"))
        }

        withContext(Dispatchers.IO) {
            mockWebServer.start()
        }

        val response = HttpLoader().get(mockWebServer.url("user-agent"))
        val json = response.toJson(deserialization = HttpBinUserAgentModel.serializer())
        assertEquals("Fuel", json.userAgent)

        withContext(Dispatchers.IO) {
            mockWebServer.shutdown()
        }
    }
}
