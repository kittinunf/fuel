package fuel

import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

internal class HttpLoaderBuilderTest {

    @Test
    fun `default okhttp settings`() {
        val mockWebServer = MockWebServer().apply {
            enqueue(MockResponse().setBody("Hello World"))
        }

        mockWebServer.start()

        val request = Request.Builder().data(mockWebServer.url("hello")).build()
        val response = HttpLoader().get(request).execute().body!!.string()
        assertEquals("Hello World", response)

        mockWebServer.shutdown()
    }

    @Test
    fun `setting connect timeouts`() {
        val mockWebServer = MockWebServer().apply {
            enqueue(MockResponse().setBody("Hello World"))
        }

        mockWebServer.start()

        val httpLoader = FuelBuilder()
            .okHttpClient {
                OkHttpClient.Builder().connectTimeout(30L, TimeUnit.MILLISECONDS).build()
            }
            .buildBlocking()

        val request = Request.Builder().data(mockWebServer.url("hello")).build()
        val response = httpLoader.get(request).execute().body!!.string()
        assertEquals("Hello World", response)

        mockWebServer.shutdown()
    }

    @Test
    fun `setting call timeouts`() {
        val mockWebServer = MockWebServer().apply {
            enqueue(MockResponse().setBody("Hello World 2"))
        }

        mockWebServer.start()

        val okhttp = OkHttpClient.Builder().callTimeout(30L, TimeUnit.MILLISECONDS).build()
        val httpLoader = FuelBuilder().okHttpClient(okhttp).buildBlocking()
        val request = Request.Builder().data(mockWebServer.url("hello2")).build()
        val response = httpLoader.get(request).execute().body!!.string()
        assertEquals("Hello World 2", response)

        mockWebServer.shutdown()
    }

    @Test
    fun `no socket response`() {
        val mockWebServer = MockWebServer().apply {
            enqueue(MockResponse().setBody("{}").setSocketPolicy(SocketPolicy.NO_RESPONSE))
        }

        mockWebServer.start()

        val request = Request.Builder().data(mockWebServer.url("socket")).build()
        try {
            HttpLoader().get(request).execute().body!!.string()
        } catch (ste: SocketTimeoutException) {
            assertEquals("timeout", ste.message)
        }

        mockWebServer.shutdown()
    }
}
