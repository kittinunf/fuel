package fuel

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import reactor.test.test
import java.lang.AssertionError

class MonoFuelTest {
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

        val request = Request.Builder().data(mockWebServer.url("url")).build()
        HttpLoader().get(request)
            .toMono()
            .test()
            .assertNext {
                assertEquals("Hello Get", it.body!!.string())
            }
            .thenCancel()
            .verify()
    }

    @Test(expected = AssertionError::class)
    fun `no socket response error`() {
        mockWebServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))

        val request = Request.Builder().data(mockWebServer.url("error")).build()
        HttpLoader().get(request)
            .toMono()
            .test()
            .assertNext {
                assertEquals("", it.message)
            }
            .thenCancel()
            .verify()
    }
}