package fuel

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

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
        val testObserver = HttpLoader().get(request)
            .toSingle()
            .test()
            .assertNoErrors()
            .assertValueCount(1)
            .assertComplete()
        val response = testObserver.values()[0]
        assertEquals("Hello Get", response.body!!.string())

        // clean up
        testObserver.dispose()
    }
}
