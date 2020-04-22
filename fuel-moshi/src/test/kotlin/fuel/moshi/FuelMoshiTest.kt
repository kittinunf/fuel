package fuel.moshi

import com.squareup.moshi.JsonClass
import fuel.HttpLoader
import fuel.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test

class FuelMoshiTest {
    @JsonClass(generateAdapter = true)
    data class HttpBinUserAgentModel(var userAgent: String = "")

    @Test
    fun testMoshiResponse() = runBlocking {
        val mockWebServer = MockWebServer().apply {
            enqueue(MockResponse().setBody("{\"userAgent\": \"Fuel\"}"))
        }

        withContext(Dispatchers.IO) {
            mockWebServer.start()
        }

        val response = HttpLoader().get(mockWebServer.url("user-agent"))
        val moshi = response.toMoshi(HttpBinUserAgentModel::class.java)!!
        assertEquals("Fuel", moshi.userAgent)

        withContext(Dispatchers.IO) {
            mockWebServer.shutdown()
        }
    }
}
