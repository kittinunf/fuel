package fuel

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

internal class FuelBlockingsTest {

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
    fun `get with url`() {
        mockWebServer.enqueue(MockResponse().setBody("Hello Get"))

        val path = mockWebServer.url("get")

        val response = Fuel.getBlocking(path)

        assertEquals("Hello Get", response.body!!.string())
    }

    @Test
    fun `get with path`() {
        mockWebServer.enqueue(MockResponse().setBody("Hello Get"))

        val path = mockWebServer.url("get")

        val response = Fuel.getBlocking(path.toString())

        assertEquals("Hello Get", response.body!!.string())
    }

    @Test
    fun `post with url`() {
        mockWebServer.enqueue(MockResponse().setBody("Yes"))

        val requestBody = "Hi POST".toRequestBody("text/html".toMediaType())

        val path = mockWebServer.url("post")

        val response = Fuel.postBlocking(path, requestBody)

        val request = mockWebServer.takeRequest()

        assertEquals("POST", request.method)
        val utf8 = request.body.readUtf8()
        assertEquals("Hi POST", utf8)
        assertEquals("Yes", response.body!!.string())
    }

    @Test
    fun `post with path`() {
        mockWebServer.enqueue(MockResponse().setBody("Yes"))

        val requestBody = "Hi POST".toRequestBody("text/html".toMediaType())

        val path = mockWebServer.url("post")

        val response = Fuel.postBlocking(path.toString(), requestBody)

        val request = mockWebServer.takeRequest()

        assertEquals("POST", request.method)
        val utf8 = request.body.readUtf8()
        assertEquals("Hi POST", utf8)
        assertEquals("Yes", response.body!!.string())
    }

    @Test
    fun `put with url`() {
        mockWebServer.enqueue(MockResponse().setBody("Yes"))

        val requestBody = "Put There".toRequestBody("text/html".toMediaType())

        val path = mockWebServer.url("put")
        val response = Fuel.putBlocking(path, requestBody)

        val request1 = mockWebServer.takeRequest()

        assertEquals("PUT", request1.method)
        val utf8 = request1.body.readUtf8()
        assertEquals("Put There", utf8)
        assertEquals("Yes", response.body!!.string())
    }

    @Test
    fun `put with path`() {
        mockWebServer.enqueue(MockResponse().setBody("Yes"))

        val requestBody = "Put There".toRequestBody("text/html".toMediaType())

        val path = mockWebServer.url("put")
        val response = Fuel.putBlocking(path.toString(), requestBody)

        val request1 = mockWebServer.takeRequest()

        assertEquals("PUT", request1.method)
        val utf8 = request1.body.readUtf8()
        assertEquals("Put There", utf8)
        assertEquals("Yes", response.body!!.string())
    }

    @Test
    fun `patch with url`() {
        mockWebServer.enqueue(MockResponse().setBody("Yes"))

        val requestBody = "Hello There".toRequestBody("text/html".toMediaType())

        val path = mockWebServer.url("patch")
        val response = Fuel.patchBlocking(path, requestBody)

        val request1 = mockWebServer.takeRequest()

        assertEquals("PATCH", request1.method)
        val utf8 = request1.body.readUtf8()
        assertEquals("Hello There", utf8)
        assertEquals("Yes", response.body!!.string())
    }

    @Test
    fun `patch with path`() {
        mockWebServer.enqueue(MockResponse().setBody("Yes"))

        val requestBody = "Hello There".toRequestBody("text/html".toMediaType())

        val path = mockWebServer.url("patch")
        val response = Fuel.patchBlocking(path.toString(), requestBody)

        val request1 = mockWebServer.takeRequest()

        assertEquals("PATCH", request1.method)
        val utf8 = request1.body.readUtf8()
        assertEquals("Hello There", utf8)
        assertEquals("Yes", response.body!!.string())
    }

    @Test
    fun `delete with url`() {
        mockWebServer.enqueue(MockResponse().setBody("Hello Delete"))

        val path = mockWebServer.url("delete")
        val response = Fuel.deleteBlocking(path, null)

        val request = mockWebServer.takeRequest()

        assertEquals("DELETE", request.method)
        assertEquals("Hello Delete", response.body!!.string())
    }

    @Test
    fun `delete with path`() {
        mockWebServer.enqueue(MockResponse().setBody("Hello Delete"))

        val path = mockWebServer.url("delete")
        val response = Fuel.deleteBlocking(path.toString(), null)

        val request = mockWebServer.takeRequest()

        assertEquals("DELETE", request.method)
        assertEquals("Hello Delete", response.body!!.string())
    }
}
