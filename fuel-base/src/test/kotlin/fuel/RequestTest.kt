package fuel

import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Test

internal class RequestTest {

    @Test
    fun `okhttp headers`() {
        val header = Headers.Builder()
            .add("X-Test", "true")
            .build()
        val request = Request.Builder()
            .data("http://example.com")
            .headers(header)
            .build()
        assertEquals("true", request.headers["X-Test"])
    }

    @Test
    fun `add headers`() {
        val request = Request.Builder()
            .data("http://example.com")
            .addHeader("X-Test", "true")
            .build()
        assertEquals("true", request.headers["X-Test"])
    }

    @Test
    fun `set headers`() {
        val request = Request.Builder()
            .data("http://example.com")
            .setHeader("X-Test", "true")
            .build()
        assertEquals("true", request.headers["X-Test"])
    }

    @Test
    fun `remove headers`() {
        val request = Request.Builder()
            .data("http://example.com")
            .setHeader("X-Test", "true")
            .removeHeader("X-Test")
            .build()
        assertEquals(null, request.headers["X-Test"])
    }

    @Test
    fun `errors when data is empty`() {
        try {
            Request.Builder().build()
        } catch (ise: IllegalStateException) {
            assertEquals("data == null", ise.message)
        }
    }

    @Test
    fun `invalid web socket as non secure url`() {
        val request = Request.Builder()
            .data("ws://google.com")
            .build()
        assertEquals("http://google.com".toHttpUrl(), request.data)
    }

    @Test
    fun `invalid web socket as secure url`() {
        val request = Request.Builder()
            .data("wss://google.com")
            .build()
        assertEquals("https://google.com".toHttpUrl(), request.data)
    }
}
