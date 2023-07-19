package fuel

import kotlin.test.Test
import kotlin.test.assertEquals
internal class RequestTest {
    @Test
    fun testAdd_headers() {
        val request = Request.Builder().apply {
            url = "http://example.com"
            headers = mapOf("X-Test" to "true")
        }.build()
        assertEquals("true", request.headers?.get("X-Test"))
    }

    @Test
    fun test_errorsWhenDataIsEmpty() {
        try {
            Request.Builder().build()
        } catch (ise: IllegalStateException) {
            assertEquals("url == null", ise.message)
        }
    }

    /*@Test
    fun `invalid web socket as non secure url`() {
        val request = Request.Builder()
            .url("ws://google.com")
            .build()
        assertEquals("http://google.com", request.url)
    }

    @Test
    fun `invalid web socket as secure url`() {
        val request = Request.Builder()
            .url("wss://google.com")
            .build()
        assertEquals("https://google.com", request.url)
    }*/
}
