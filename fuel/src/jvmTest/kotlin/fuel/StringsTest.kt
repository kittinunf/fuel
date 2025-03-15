package fuel

import kotlinx.coroutines.runBlocking
import kotlinx.io.readString
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.ExperimentalOkHttpApi
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalOkHttpApi::class)
class StringsTest {
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun `before test`() {
        Fuel.setHttpLoader(JVMHttpLoader(lazyOf(OkHttpClient())))
        mockWebServer = MockWebServer().apply { start() }
    }

    @After
    fun `after test`() {
        mockWebServer.shutdown()
    }

    @Test
    fun `get test data`() =
        runBlocking {
            mockWebServer.enqueue(MockResponse(body = "Hello World"))

            val string =
                mockWebServer
                    .url("get")
                    .toString()
                    .httpGet()
                    .source
                    .readString()
            val request2 = mockWebServer.takeRequest()

            assertEquals("GET", request2.method)
            assertEquals(string, "Hello World")
        }

    @Test
    fun `post test data`() =
        runBlocking {
            mockWebServer.enqueue(MockResponse())

            mockWebServer.url("post").toString().httpPost(body = "Hi?")
            val request1 = mockWebServer.takeRequest()

            assertEquals("POST", request1.method)
            val utf8 = request1.body.readUtf8()
            assertEquals("Hi?", utf8)
        }

    @Test(expected = IllegalArgumentException::class)
    fun `empty response body for post`() =
        runBlocking {
            mockWebServer.enqueue(MockResponse())

            mockWebServer.url("post").toString().httpPost()
            val request1 = mockWebServer.takeRequest()

            assertEquals("POST", request1.method)
        }

    @Test
    fun `put test data`() =
        runBlocking {
            mockWebServer.enqueue(MockResponse())

            mockWebServer.url("put").toString().httpPut(body = "Put There")
            val request1 = mockWebServer.takeRequest()

            assertEquals("PUT", request1.method)
            val utf8 = request1.body.readUtf8()
            assertEquals("Put There", utf8)
        }

    @Test(expected = IllegalArgumentException::class)
    fun `empty response body for put`() =
        runBlocking {
            mockWebServer.enqueue(MockResponse())

            mockWebServer.url("put").toString().httpPut()
            val request1 = mockWebServer.takeRequest()

            assertEquals("PUT", request1.method)
        }

    @Test
    fun `patch test data`() =
        runBlocking {
            mockWebServer.enqueue(MockResponse())

            mockWebServer.url("patch").toString().httpPatch(body = "Hello There")
            val request1 = mockWebServer.takeRequest()

            assertEquals("PATCH", request1.method)
            val utf8 = request1.body.readUtf8()
            assertEquals("Hello There", utf8)
        }

    @Test(expected = IllegalArgumentException::class)
    fun `empty response body for patch`() =
        runBlocking {
            mockWebServer.enqueue(MockResponse())

            mockWebServer.url("patch").toString().httpPatch()
            val request1 = mockWebServer.takeRequest()

            assertEquals("PATCH", request1.method)
        }

    @Test
    fun `delete test data`() =
        runBlocking {
            mockWebServer.enqueue(MockResponse(body = "Hello World"))

            val string =
                mockWebServer
                    .url("delete")
                    .toString()
                    .httpDelete()
                    .source
                    .readString()
            val request1 = mockWebServer.takeRequest()

            assertEquals("DELETE", request1.method)
            assertEquals(string, "Hello World")
        }

    @Test
    fun `head test data`() =
        runBlocking {
            mockWebServer.enqueue(MockResponse())

            mockWebServer.url("head").toString().httpHead()
            val request1 = mockWebServer.takeRequest()

            assertEquals("HEAD", request1.method)
        }

    @Test
    fun `connect test data`() =
        runBlocking {
            mockWebServer.enqueue(MockResponse())

            mockWebServer.url("connect").toString().httpMethod(method = "CONNECT")
            val request1 = mockWebServer.takeRequest()

            assertEquals("CONNECT", request1.method)
        }
}
