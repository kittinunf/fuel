package fuel

import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.junit.Assert.assertEquals
import org.junit.Test

class RequestTest {
    @Test
    fun `okhttp headers`() {
        val header = Headers.Builder()
            .add("X-Test", "true")
            .build()
        val request = Request.Builder()
            .data("http://example.com".toHttpUrlOrNull())
            .headers(header)
            .build()
        assertEquals("true", request.headers.get("X-Test"))
    }

    @Test
    fun `add headers`() {
        val request = Request.Builder()
            .data("http://example.com".toHttpUrlOrNull())
            .addHeader("X-Test", "true")
            .build()
        assertEquals("true", request.headers.get("X-Test"))
    }

    @Test
    fun `set headers`() {
        val request = Request.Builder()
            .data("http://example.com".toHttpUrlOrNull())
            .setHeader("X-Test", "true")
            .build()
        assertEquals("true", request.headers.get("X-Test"))
    }

    @Test
    fun `remove headers`() {
        val request = Request.Builder()
            .data("http://example.com".toHttpUrlOrNull())
            .setHeader("X-Test", "true")
            .removeHeader("X-Test")
            .build()
        assertEquals(null, request.headers.get("X-Test"))
    }

    @Test
    fun `errors when data is empty`() {
        try {
            Request.Builder().build()
        } catch (ise: IllegalStateException) {
            assertEquals("data == null", ise.message)
        }
    }
}
