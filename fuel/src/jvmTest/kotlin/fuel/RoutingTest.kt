package fuel

import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RoutingTest {

    sealed class TestApi(private val host: String) : FuelRouting {
        override val basePath = this.host

        class GetTest(host: String) : TestApi(host)
        class GetParamsTest(host: String) : TestApi(host)

        override val parameters: Parameters?
            get() = null

        override val method: String
            get() {
                return when (this) {
                    is GetTest -> "GET"
                    is GetParamsTest -> "GET"
                }
            }

        override val path: String
            get() {
                return when (this) {
                    is GetTest -> "/get"
                    is GetParamsTest -> "/get?foo=bar"
                }
            }

        override val body: String?
            get() = null

        override val headers: Map<String, String>?
            get() {
                return when (this) {
                    is GetTest -> null
                    is GetParamsTest -> mapOf("X-Test" to "true")
                }
            }
    }

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer().apply { start() }
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun httpRouterGet() = runBlocking {
        mockWebServer.enqueue(MockResponse().setBody("Hello World"))

        val getTest = TestApi.GetTest(mockWebServer.url("").toString())
        val response = Fuel.request(getTest).body.string()
        val request1 = mockWebServer.takeRequest()

        assertEquals("Hello World", response)
        assertEquals("GET", request1.method)
    }

    @Test
    fun httpRouterGetParams() = runBlocking {
        mockWebServer.enqueue(MockResponse().setBody("Hello World With Params"))

        val getTest = TestApi.GetParamsTest(mockWebServer.url("").toString())
        val response = Fuel.request(getTest).body.string()
        val request1 = mockWebServer.takeRequest()

        assertEquals("Hello World With Params", response)
        assertEquals("GET", request1.method)
        assertEquals("///get?foo=bar", request1.path)
    }
}
