package fuel.jackson

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import fuel.Fuel
import fuel.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test

class FuelJacksonTest {
    data class HttpBinUserAgentModel(var userAgent: String = "", var http_status: String = "")

    @Test
    fun jacksonTestResponseObject() = runBlocking {
        val mockWebServer = MockWebServer().apply {
            enqueue(MockResponse().setBody("{\"userAgent\": \"Fuel\"}"))
        }

        withContext(Dispatchers.IO) {
            mockWebServer.start()
        }

        val response = Fuel.get(mockWebServer.url("user-agent"))
        val jackson = response.toJackson<HttpBinUserAgentModel>()
        assertEquals("Fuel", jackson.userAgent)

        withContext(Dispatchers.IO) {
            mockWebServer.shutdown()
        }
    }

    @Test
    fun jacksonTestResponseObjectWithCustomMapper() = runBlocking {
        val mockWebServer = MockWebServer().apply {
            enqueue(MockResponse().setBody("{\"userAgent\": \"Fuel\", \"http_status\": \"OK\"}"))
        }

        withContext(Dispatchers.IO) {
            mockWebServer.start()
        }

        val response = Fuel.get(mockWebServer.url("user-agent"))
        val jackson = response.toJackson<HttpBinUserAgentModel>(createCustomMapper())
        assertEquals("", jackson.userAgent)
        assertEquals("OK", jackson.http_status)

        withContext(Dispatchers.IO) {
            mockWebServer.shutdown()
        }
    }

    private fun createCustomMapper(): ObjectMapper = ObjectMapper().registerKotlinModule()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).apply {
            propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
        }
}
