package fuel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.RequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

internal class RoutingTest {

    sealed class TestApi(private val host: HttpUrl) : FuelRouting {
        override val basePath = this.host.toString()

        class GetTest(host: HttpUrl) : TestApi(host)
        class GetParamsTest(host: HttpUrl) : TestApi(host)

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

        override val body: RequestBody?
            get() = null

        override val headers: Headers.Builder?
            get() {
                return when (this) {
                    is GetTest -> null
                    is GetParamsTest -> Headers.Builder().add("X-Test", "true")
                }
            }
    }

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun httpRouterGet() = runBlocking {
        mockWebServer.enqueue(MockResponse().setBody("Hello World"))

        withContext(Dispatchers.IO) {
            mockWebServer.start()
        }

        val getTest = TestApi.GetTest(mockWebServer.url(""))
        val response = SuspendHttpLoader().method(getTest.request).body!!.string()

        val request1 = withContext(Dispatchers.IO) {
            mockWebServer.takeRequest()
        }

        assertEquals("Hello World", response)
        assertEquals("GET", request1.method)
    }

    @Test
    fun httpRouterGetParams() = runBlocking {
        mockWebServer.enqueue(MockResponse().setBody("Hello World With Params"))

        withContext(Dispatchers.IO) {
            mockWebServer.start()
        }

        val getTest = TestApi.GetParamsTest(mockWebServer.url(""))
        val response = SuspendHttpLoader().method(getTest.request).body!!.string()

        val request1 = withContext(Dispatchers.IO) {
            mockWebServer.takeRequest()
        }

        assertEquals("Hello World With Params", response)
        assertEquals("GET", request1.method)
        assertEquals("///get?foo=bar", request1.path)
    }
}
