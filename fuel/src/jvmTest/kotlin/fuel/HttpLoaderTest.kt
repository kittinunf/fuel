package fuel

import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

internal class HttpLoaderTest {
    private lateinit var httpLoader: HttpLoader
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun `before test`() {
        httpLoader = JVMHttpLoader(lazyOf(OkHttpClient()))
        mockWebServer = MockWebServer().apply { start() }
    }

    @After
    fun `after test`() {
        mockWebServer.shutdown()
    }

    @Test
    fun `unsuccessful 404 Error`() = runBlocking {
        mockWebServer.enqueue(MockResponse().setResponseCode(404).setBody("Hello World"))

        val unsuccessfulRequest = Request.Builder().url(mockWebServer.url("get").toString()).build()

        val string = httpLoader.get(unsuccessfulRequest).body

        val request1 = mockWebServer.takeRequest()

        assertEquals("GET", request1.method)
        assertEquals(string, "Hello World")
    }

    @Test
    fun `get test data`() = runBlocking {
        mockWebServer.enqueue(MockResponse().setBody("Hello World"))

        val request = Request.Builder().url(mockWebServer.url("get").toString()).build()

        val string = httpLoader.get(request).body

        val request1 = mockWebServer.takeRequest()

        assertEquals("GET", request1.method)
        assertEquals(string, "Hello World")
    }

    @Test
    fun `post test data`() = runBlocking {
        mockWebServer.enqueue(MockResponse())

        val request = Request.Builder()
            .url(mockWebServer.url("post").toString())
            .body("Hi?")
            .build()

        httpLoader.post(request)
        val request1 = mockWebServer.takeRequest()

        assertEquals("POST", request1.method)
        val utf8 = request1.body.readUtf8()
        assertEquals("Hi?", utf8)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty response body for post`() = runBlocking {
        mockWebServer.enqueue(MockResponse())

        val request = Request.Builder().url(mockWebServer.url("post").toString()).build()

        httpLoader.post(request)

        val request1 = mockWebServer.takeRequest()
        assertEquals("POST", request1.method)
    }

    @Test
    fun `put test data`() = runBlocking {
        mockWebServer.enqueue(MockResponse())

        val request = Request.Builder()
            .url(mockWebServer.url("put").toString())
            .body("Put There")
            .build()

        httpLoader.put(request)

        val request1 = mockWebServer.takeRequest()

        assertEquals("PUT", request1.method)
        val utf8 = request1.body.readUtf8()
        assertEquals("Put There", utf8)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty response body for put`() = runBlocking {
        mockWebServer.enqueue(MockResponse())

        val request = Request.Builder().url(mockWebServer.url("put").toString()).build()

        httpLoader.put(request)

        val request1 = mockWebServer.takeRequest()

        assertEquals("PUT", request1.method)
    }

    @Test
    fun `patch test data`() = runBlocking {
        mockWebServer.enqueue(MockResponse())

        val request = Request.Builder()
            .url(mockWebServer.url("patch").toString())
            .body("Hello There")
            .build()

        httpLoader.patch(request)

        val request1 = mockWebServer.takeRequest()

        assertEquals("PATCH", request1.method)
        val utf8 = request1.body.readUtf8()
        assertEquals("Hello There", utf8)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty response body for patch`() = runBlocking {
        mockWebServer.enqueue(MockResponse())

        val request = Request.Builder().url(mockWebServer.url("patch").toString()).build()

        httpLoader.patch(request)

        val request1 = mockWebServer.takeRequest()

        assertEquals("PATCH", request1.method)
    }

    @Test
    fun `delete test data`() = runBlocking {
        mockWebServer.enqueue(MockResponse().setBody("Hello World"))

        val request = Request.Builder()
            .url(mockWebServer.url("delete").toString())
            .build()

        val string = httpLoader.delete(request).body

        val request1 = mockWebServer.takeRequest()

        assertEquals("DELETE", request1.method)
        assertEquals(string, "Hello World")
    }

    @Test
    fun `head test data`() = runBlocking {
        mockWebServer.enqueue(MockResponse())

        val request = Request.Builder().url(mockWebServer.url("head").toString()).build()

        httpLoader.head(request)

        val request1 = mockWebServer.takeRequest()

        assertEquals("HEAD", request1.method)
    }

    @Test
    fun `connect test data`() = runBlocking {
        mockWebServer.enqueue(MockResponse())

        val request = Request.Builder()
            .url(mockWebServer.url("connect").toString())
            .method("CONNECT")
            .build()

        httpLoader.method(request)

        val request1 = mockWebServer.takeRequest()

        assertEquals("CONNECT", request1.method)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty method for CONNECT`() = runBlocking {
        mockWebServer.enqueue(MockResponse())

        val request = Request.Builder().url(mockWebServer.url("connect").toString()).build()

        httpLoader.method(request)

        val request1 = mockWebServer.takeRequest()

        assertEquals("CONNECT", request1.method)
    }
}
