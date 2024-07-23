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
class HttpLoaderTest {
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
    fun `unsuccessful 404 Error`() =
        runBlocking {
            mockWebServer.enqueue(MockResponse(code = 404, body = "Hello World"))

            val string =
                httpLoader.get {
                    url = mockWebServer.url("get").toString()
                }.source.readString()

            val request1 = mockWebServer.takeRequest()

            assertEquals("GET", request1.method)
            assertEquals(string, "Hello World")
        }

    @Test
    fun `get test data`() =
        runBlocking {
            mockWebServer.enqueue(MockResponse(body = "Hello World"))

            val string =
                httpLoader.get {
                    url = mockWebServer.url("get").toString()
                }.source.readString()

            val request2 = mockWebServer.takeRequest()

            assertEquals("GET", request2.method)
            assertEquals(string, "Hello World")
        }

    @Test
    fun `get test data with parameters`() =
        runBlocking {
            mockWebServer.enqueue(MockResponse(body = "Hello There"))

            val string =
                httpLoader.get {
                    url = mockWebServer.url("get").toString()
                    parameters = listOf("foo" to "bar")
                }.source.readString()

            val request1 = mockWebServer.takeRequest()

            assertEquals("GET", request1.method)
            assertEquals(string, "Hello There")
        }

    @Test
    fun `get test data with headers`() =
        runBlocking {
            mockWebServer.enqueue(MockResponse(body = "Greeting Everyone"))

            val string =
                httpLoader.get {
                    url = mockWebServer.url("get").toString()
                    headers = mapOf("foo" to "bar")
                }.source.readString()

            val request1 = mockWebServer.takeRequest()

            assertEquals("GET", request1.method)
            assertEquals(string, "Greeting Everyone")
        }

    @Test
    fun `post test data`() =
        runBlocking {
            mockWebServer.enqueue(MockResponse())

            httpLoader.post {
                url = mockWebServer.url("post").toString()
                body = "Hi?"
            }
            val request1 = mockWebServer.takeRequest()

            assertEquals("POST", request1.method)
            val utf8 = request1.body.readUtf8()
            assertEquals("Hi?", utf8)
        }

    @Test(expected = IllegalArgumentException::class)
    fun `empty response body for post`() =
        runBlocking {
            mockWebServer.enqueue(MockResponse())

            httpLoader.post {
                url = mockWebServer.url("post").toString()
            }

            val request1 = mockWebServer.takeRequest()
            assertEquals("POST", request1.method)
        }

    @Test
    fun `put test data`() =
        runBlocking {
            mockWebServer.enqueue(MockResponse())

            httpLoader.put {
                url = mockWebServer.url("put").toString()
                body = "Put There"
            }

            val request1 = mockWebServer.takeRequest()

            assertEquals("PUT", request1.method)
            val utf8 = request1.body.readUtf8()
            assertEquals("Put There", utf8)
        }

    @Test(expected = IllegalArgumentException::class)
    fun `empty response body for put`() =
        runBlocking {
            mockWebServer.enqueue(MockResponse())

            httpLoader.put {
                url = mockWebServer.url("put").toString()
            }

            val request1 = mockWebServer.takeRequest()

            assertEquals("PUT", request1.method)
        }

    @Test
    fun `patch test data`() =
        runBlocking {
            mockWebServer.enqueue(MockResponse())

            httpLoader.patch {
                url = mockWebServer.url("patch").toString()
                body = "Hello There"
            }

            val request1 = mockWebServer.takeRequest()

            assertEquals("PATCH", request1.method)
            val utf8 = request1.body.readUtf8()
            assertEquals("Hello There", utf8)
        }

    @Test(expected = IllegalArgumentException::class)
    fun `empty response body for patch`() =
        runBlocking {
            mockWebServer.enqueue(MockResponse())

            httpLoader.patch {
                url = mockWebServer.url("patch").toString()
            }

            val request1 = mockWebServer.takeRequest()

            assertEquals("PATCH", request1.method)
        }

    @Test
    fun `delete test data`() =
        runBlocking {
            mockWebServer.enqueue(MockResponse(body = "Hello World"))

            val string =
                httpLoader.delete {
                    url = mockWebServer.url("delete").toString()
                }.source.readString()

            val request1 = mockWebServer.takeRequest()

            assertEquals("DELETE", request1.method)
            assertEquals(string, "Hello World")
        }

    @Test
    fun `head test data`() =
        runBlocking {
            mockWebServer.enqueue(MockResponse())

            httpLoader.head {
                url = mockWebServer.url("head").toString()
            }

            val request1 = mockWebServer.takeRequest()

            assertEquals("HEAD", request1.method)
        }

    @Test
    fun `connect test data`() =
        runBlocking {
            mockWebServer.enqueue(MockResponse())

            httpLoader.method {
                url = mockWebServer.url("connect").toString()
                method = "CONNECT"
            }

            val request1 = mockWebServer.takeRequest()

            assertEquals("CONNECT", request1.method)
        }

    @Test(expected = IllegalArgumentException::class)
    fun `empty method for CONNECT`() =
        runBlocking {
            mockWebServer.enqueue(MockResponse())

            httpLoader.method {
                url = mockWebServer.url("connect").toString()
            }

            val request1 = mockWebServer.takeRequest()

            assertEquals("CONNECT", request1.method)
        }
}
