package fuel.jackson

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import fuel.Fuel
import fuel.get
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.ExperimentalOkHttpApi
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

@ExperimentalOkHttpApi
class FuelJacksonTest {
    private val createCustomMapper: ObjectMapper =
        ObjectMapper().registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).apply {
                propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
            }

    data class HttpBinUserAgentModel(
        val userAgent: String = "",
        val http_status: String = "",
    )

    @Test
    fun jacksonTestResponseObject() =
        runBlocking {
            val mockWebServer =
                MockWebServer().apply {
                    enqueue(MockResponse(body = "{\"userAgent\": \"Fuel\"}"))
                    start()
                }

            val response = Fuel.get(mockWebServer.url("user-agent").toString())
            val jackson = response.toJackson<HttpBinUserAgentModel>()
            jackson.fold({
                assertEquals("Fuel", it?.userAgent)
            }, {
                fail(it.localizedMessage)
            })

            mockWebServer.shutdown()
        }

    @Test
    fun jacksonTestResponseObjectWithCustomMapper() =
        runBlocking {
            val mockWebServer =
                MockWebServer().apply {
                    enqueue(MockResponse(body = "{\"userAgent\": \"Fuel\", \"http_status\": \"OK\"}"))
                    start()
                }

            val response = Fuel.get(mockWebServer.url("user-agent").toString())
            val jackson = response.toJackson<HttpBinUserAgentModel>(createCustomMapper)
            jackson.fold({
                assertEquals("", it?.userAgent)
                assertEquals("OK", it?.http_status)
            }, {
                fail(it.localizedMessage)
            })

            mockWebServer.shutdown()
        }
}
