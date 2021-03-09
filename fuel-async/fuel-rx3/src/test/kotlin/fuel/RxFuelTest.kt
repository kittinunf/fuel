package fuel

Hm, wimport io.reactivex.rxjava3.schedulers.Schedulers
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.lang.AssertionError

class RxFuelTest {
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
            .toSingle()
            .subscribeOn(Schedulers.io())
            .doOnSuccess {
                assertEquals("Hello Get", it.body!!.string())
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

        // clean up
        testObserver.dispose()
    }
}
