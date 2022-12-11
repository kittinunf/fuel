package fuel

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

internal class RealHttpLoaderTest {

    private lateinit var realHttpLoader: RealHttpLoader
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun `before test`() {
        realHttpLoader = RealHttpLoader(OkHttpClient())

        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun `after test`() {
        mockWebServer.shutdown()
    }

    @Test(expected = HttpException::class)
    fun `unsuccessful 404 Error`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(404).setBody("Hello World"))

        val unsuccessfulRequest = Request.Builder().data(mockWebServer.url("get")).build()

        val string = realHttpLoader.get(unsuccessfulRequest).response().body.string()

        val request1 = mockWebServer.takeRequest()

        assertEquals("GET", request1.method)
        assertEquals(string, "Hello World")
    }

    @Test
    fun `get test data`() {
        mockWebServer.enqueue(MockResponse().setBody("Hello World"))

        val request = Request.Builder().data(mockWebServer.url("get")).build()

        val string = realHttpLoader.get(request).response().body.string()

        val request1 = mockWebServer.takeRequest()

        assertEquals("GET", request1.method)
        assertEquals(string, "Hello World")
    }

    @Test
    fun `post test data`() {
        mockWebServer.enqueue(MockResponse())

        val requestBody = "Hi?".toRequestBody("text/html".toMediaType())
        val request = Request.Builder()
            .data(mockWebServer.url("post"))
            .requestBody(requestBody)
            .build()

        realHttpLoader.post(request).response()
        val request1 = mockWebServer.takeRequest()

        assertEquals("POST", request1.method)
        val utf8 = request1.body.readUtf8()
        assertEquals("Hi?", utf8)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty response body for post`() {
        mockWebServer.enqueue(MockResponse())

        val request = Request.Builder().data(mockWebServer.url("post")).build()

        realHttpLoader.post(request).response()

        val request1 = mockWebServer.takeRequest()

        assertEquals("POST", request1.method)
    }

    @Test
    fun `put test data`() {
        mockWebServer.enqueue(MockResponse())

        val requestBody = "Put There".toRequestBody("text/html".toMediaType())
        val request = Request.Builder()
            .data(mockWebServer.url("put"))
            .requestBody(requestBody)
            .build()

        realHttpLoader.put(request).response()

        val request1 = mockWebServer.takeRequest()

        assertEquals("PUT", request1.method)
        val utf8 = request1.body.readUtf8()
        assertEquals("Put There", utf8)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty response body for put`() {
        mockWebServer.enqueue(MockResponse())

        val request = Request.Builder().data(mockWebServer.url("put")).build()

        realHttpLoader.put(request).response()

        val request1 = mockWebServer.takeRequest()

        assertEquals("PUT", request1.method)
    }

    @Test
    fun `patch test data`() {
        mockWebServer.enqueue(MockResponse())

        val requestBody = "Hello There".toRequestBody("text/html".toMediaType())
        val request = Request.Builder()
            .data(mockWebServer.url("patch"))
            .requestBody(requestBody)
            .build()

        realHttpLoader.patch(request).response()

        val request1 = mockWebServer.takeRequest()

        assertEquals("PATCH", request1.method)
        val utf8 = request1.body.readUtf8()
        assertEquals("Hello There", utf8)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty response body for patch`() {
        mockWebServer.enqueue(MockResponse())

        val request = Request.Builder().data(mockWebServer.url("patch")).build()

        realHttpLoader.patch(request).response()

        val request1 = mockWebServer.takeRequest()

        assertEquals("PATCH", request1.method)
    }

    @Test
    fun `delete test data`() {
        mockWebServer.enqueue(MockResponse().setBody("Hello World"))

        val request = Request.Builder()
            .data(mockWebServer.url("delete"))
            .requestBody(null)
            .build()

        val string = realHttpLoader.delete(request).response().body.string()

        val request1 = mockWebServer.takeRequest()

        assertEquals("DELETE", request1.method)
        assertEquals(string, "Hello World")
    }

    @Test
    fun `head test data`() {
        mockWebServer.enqueue(MockResponse())

        val request = Request.Builder().data(mockWebServer.url("head")).build()

        realHttpLoader.head(request).response()

        val request1 = mockWebServer.takeRequest()

        assertEquals("HEAD", request1.method)
    }

    @Test
    fun `connect test data`() {
        mockWebServer.enqueue(MockResponse())

        val request = Request.Builder()
            .data(mockWebServer.url("connect"))
            .method("CONNECT")
            .requestBody(null)
            .build()

        realHttpLoader.method(request).response()

        val request1 = mockWebServer.takeRequest()

        assertEquals("CONNECT", request1.method)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty method for CONNECT`() {
        mockWebServer.enqueue(MockResponse())

        val request = Request.Builder().data(mockWebServer.url("connect")).build()

        realHttpLoader.method(request).response()

        val request1 = mockWebServer.takeRequest()

        assertEquals("CONNECT", request1.method)
    }
}
