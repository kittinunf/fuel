package fuel.jackson

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import fuel.Fuel
import fuel.get
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test

internal class FuelJacksonTest {

    data class HttpBinUserAgentModel(var userAgent: String = "", var http_status: String = "")

    @Test
    fun jacksonTestResponseObject() {
        val mockWebServer = MockWebServer().apply {
            enqueue(MockResponse().setBody("{\"userAgent\": \"Fuel\"}"))
            start()
        }

        val response = Fuel.get(mockWebServer.url("user-agent")).execute()
        val jackson = response.toJackson<HttpBinUserAgentModel>()
        assertEquals("Fuel", jackson.userAgent)

        mockWebServer.shutdown()
    }

    @Test
    fun jacksonTestResponseObjectWithCustomMapper() {
        val mockWebServer = MockWebServer().apply {
            enqueue(MockResponse().setBody("{\"userAgent\": \"Fuel\", \"http_status\": \"OK\"}"))
            start()
        }

        val response = Fuel.get(mockWebServer.url("user-agent")).execute()
        val jackson = response.toJackson<HttpBinUserAgentModel>(createCustomMapper())
        assertEquals("", jackson.userAgent)
        assertEquals("OK", jackson.http_status)

        mockWebServer.shutdown()
    }

    private fun createCustomMapper(): ObjectMapper = ObjectMapper().registerKotlinModule()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).apply {
            propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
        }
}
