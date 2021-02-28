package fuel

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

internal class SuspendHttpLoaderBuilderTest {

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun `before test`() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun `after test`() {
        mockWebServer.shutdown()
    }

    @Test
    fun `default okhttp settings`() = runBlocking {
        mockWebServer.enqueue(MockResponse().setBody("Hello World"))

        val request = Request.Builder().data(mockWebServer.url("hello")).build()
        val response = SuspendHttpLoader().get(request).body!!.string()
        assertEquals("Hello World", response)
    }

    @Test
    fun `setting connect timeouts`() = runBlocking {
        mockWebServer.enqueue(MockResponse().setBody("Hello World"))

        val httpLoader = FuelBuilder()
            .okHttpClient {
                OkHttpClient.Builder().connectTimeout(30L, TimeUnit.MILLISECONDS).build()
            }
            .build()
        val request = Request.Builder().data(mockWebServer.url("hello")).build()
        val response = httpLoader.get(request).body!!.string()
        assertEquals("Hello World", response)
    }

    @Test
    fun `setting call timeouts`() = runBlocking {
        mockWebServer.enqueue(MockResponse().setBody("Hello World 2"))

        val okhttp = OkHttpClient.Builder().callTimeout(30L, TimeUnit.MILLISECONDS).build()
        val httpLoader = FuelBuilder().okHttpClient(okhttp).build()
        val request = Request.Builder().data(mockWebServer.url("hello2")).build()
        val response = httpLoader.get(request).body!!.string()
        assertEquals("Hello World 2", response)
    }

    @Test
    fun `no socket response`() {
        runBlocking {
            mockWebServer.enqueue(MockResponse().setBody("{}").setSocketPolicy(SocketPolicy.NO_RESPONSE))

            val request = Request.Builder().data(mockWebServer.url("socket")).build()
            try {
                SuspendHttpLoader().get(request).body!!.string()
            } catch (ste: SocketTimeoutException) {
                assertEquals("timeout", ste.message)
            }
        }
    }
}
