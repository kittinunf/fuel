package fuel

import org.junit.Assert.assertEquals
import org.junit.Test

internal class RequestTest {
    @Test
    fun `add headers`() {
        val request = Request.Builder()
            .url("http://example.com")
            .headers(mapOf("X-Test" to "true"))
            .build()
        assertEquals("true", request.headers?.get("X-Test"))
    }

    @Test
    fun `errors when data is empty`() {
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
