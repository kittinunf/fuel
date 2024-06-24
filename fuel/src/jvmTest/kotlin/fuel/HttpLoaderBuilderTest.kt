package fuel

import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.SocketPolicy
import okhttp3.ExperimentalOkHttpApi
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import kotlin.test.assertNotNull

@OptIn(ExperimentalOkHttpApi::class)
class HttpLoaderBuilderTest {
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun `before test`() {
        mockWebServer = MockWebServer().apply { start() }
    }

    @After
    fun `after test`() {
        mockWebServer.shutdown()
    }

    @Test
    fun `default okhttp settings`() = runBlocking {
        mockWebServer.enqueue(MockResponse(body = "Hello World"))
        val response = JVMHttpLoader().get {
            url = mockWebServer.url("hello").toString()
        }.body.string()
        assertEquals("Hello World", response)

        mockWebServer.shutdown()
    }

    @Test
    fun `default okhttp settings with parameter`() = runBlocking {
        mockWebServer.enqueue(MockResponse(body = "Hello World 3"))
        val response = JVMHttpLoader().get {
            url = mockWebServer.url("hello").toString()
            parameters = listOf("foo" to "bar")
        }.body.string()
        assertEquals("Hello World 3", response)

        mockWebServer.shutdown()
    }

    @Test
    fun `default okhttp settings with headers`() = runBlocking {
        mockWebServer.enqueue(MockResponse(body = "Hello World"))
        val response = JVMHttpLoader().get {
            url = mockWebServer.url("hello").toString()
        }.headers
        assertEquals("1", response["Content-Length"])

        mockWebServer.shutdown()
    }

    @Test
    fun `setting connect timeouts`() = runBlocking {
        mockWebServer.enqueue(MockResponse(body = "Hello World 4"))

        val httpLoader = FuelBuilder()
            .config {
                OkHttpClient.Builder().connectTimeout(30L, TimeUnit.MILLISECONDS).build()
            }
            .build()
        val response = httpLoader.get {
            url = mockWebServer.url("hello").toString()
        }.body.string()
        assertEquals("Hello World 4", response)
    }

    @Test
    fun `setting call timeouts`() = runBlocking {
        mockWebServer.enqueue(MockResponse(body = "Hello World 5"))

        val okhttp = OkHttpClient.Builder().callTimeout(30L, TimeUnit.MILLISECONDS).build()
        val httpLoader = FuelBuilder().config(okhttp).build()
        val response = httpLoader.get {
            url = mockWebServer.url("hello2").toString()
        }.body.string()
        assertEquals("Hello World 5", response)
    }

    @Test
    fun `no socket response`(): Unit = runBlocking {
        mockWebServer.enqueue(MockResponse(body = "{}", socketPolicy = SocketPolicy.NoResponse))
        try {
            JVMHttpLoader().get {
                url = mockWebServer.url("socket").toString()
            }.body.string()
        } catch (ste: SocketTimeoutException) {
            assertNotNull(ste, "socket timeout")
        }
    }
}
