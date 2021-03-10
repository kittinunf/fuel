package fuel

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.SocketPolicy
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

internal class RxJavaTest {

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
        val testObserver = HttpLoader().get(request)
            .toSingle()
            .test()
            .assertValueCount(1)
            .assertComplete()
            .assertNoErrors()

        testObserver.dispose()
    }

    @Test
    fun `get with url and cancel mid-flight`() {
        mockWebServer.enqueue(MockResponse().setBody("Hello Get").setHeadersDelay(10, TimeUnit.SECONDS))

        val request = Request.Builder().data(mockWebServer.url("get")).build()
        val testObserver = HttpLoader().get(request)
            .toSingle()
            .test()
            .assertNoValues()
            .assertError {
                it is SocketTimeoutException
            }
    }

    @Test(expected = AssertionError::class)
    fun `no socket response error`() {
        mockWebServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))

        val request = Request.Builder().data(mockWebServer.url("error")).build()
        val testObserver = HttpLoader().get(request)
            .toSingle()
            .test()
            .assertComplete()
            .assertNoErrors()
    }
}
