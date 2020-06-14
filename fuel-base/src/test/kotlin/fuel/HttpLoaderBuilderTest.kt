package fuel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class HttpLoaderBuilderTest {
    @Test
    fun `default okhttp settings`() = runBlocking {
        val mockWebServer = MockWebServer().apply {
            enqueue(MockResponse().setBody("Hello World"))
        }

        withContext(Dispatchers.IO) {
            mockWebServer.start()
        }

        val request = Request.Builder().data(mockWebServer.url("hello")).build()
        val response = HttpLoader().get(request).body!!.string()
        assertEquals("Hello World", response)

        withContext(Dispatchers.IO) {
            mockWebServer.shutdown()
        }
    }

    @Test
    fun `setting connect timeouts`() = runBlocking {
        val mockWebServer = MockWebServer().apply {
            enqueue(MockResponse().setBody("Hello World"))
        }

        withContext(Dispatchers.IO) {
            mockWebServer.start()
        }

        val httpLoader = HttpLoader.Builder()
            .okHttpClient {
                OkHttpClient.Builder().connectTimeout(30L, TimeUnit.MILLISECONDS).build()
            }
            .build()
        val request = Request.Builder().data(mockWebServer.url("hello")).build()
        val response = httpLoader.get(request).body!!.string()
        assertEquals("Hello World", response)

        withContext(Dispatchers.IO) {
            mockWebServer.shutdown()
        }
    }

    @Test
    fun `setting call timeouts`() = runBlocking {
        val mockWebServer = MockWebServer().apply {
            enqueue(MockResponse().setBody("Hello World"))
        }

        withContext(Dispatchers.IO) {
            mockWebServer.start()
        }

        val okhttp = OkHttpClient.Builder().callTimeout(30L, TimeUnit.MILLISECONDS).build()
        val httpLoader = HttpLoader.Builder().okHttpClient(okhttp).build()
        val request = Request.Builder().data(mockWebServer.url("hello")).build()
        val response = httpLoader.get(request).body!!.string()
        assertEquals("Hello World", response)

        withContext(Dispatchers.IO) {
            mockWebServer.shutdown()
        }
    }

    @Test
    fun `no socket response`() = runBlocking {
        val mockWebServer = MockWebServer().apply {
            enqueue(MockResponse().setBody("{}").setSocketPolicy(SocketPolicy.NO_RESPONSE))
        }

        withContext(Dispatchers.IO) {
            mockWebServer.start()
        }

        val request = Request.Builder().data(mockWebServer.url("socket")).build()
        try {
            HttpLoader().get(request).body!!.string()
        } catch (ste: SocketTimeoutException) {
            assertEquals("timeout", ste.message)
        }

        withContext(Dispatchers.IO) {
            mockWebServer.shutdown()
        }
    }
}
