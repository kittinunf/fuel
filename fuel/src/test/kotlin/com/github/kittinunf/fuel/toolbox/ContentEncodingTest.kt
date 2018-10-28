package com.github.kittinunf.fuel.toolbox

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.MockHttpTestCase
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import org.mockserver.model.Header
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

class ContentEncodingTest : MockHttpTestCase() {
    @Test
    fun gzipContentEncodingTest() {
        val value = ByteArray(32).apply {
            for (i in 0..(this.size - 1)) {
                this[i] = ('A'..'z').random().toByte()
            }
        }

        val inner = ByteArrayOutputStream(value.size)
        val output = GZIPOutputStream(inner)
        output.write(value)
        output.finish()

        // It's written to here
        val gzipped = inner.toByteArray()

        mock.chain(
            request = mock.request()
                .withMethod(Method.POST.value)
                .withPath("/gzip")
                .withHeader(Header.header(Headers.ACCEPT_ENCODING, "gzip")),
            response = mock.response()
                .withHeaders(
                    Header.header(Headers.CONTENT_ENCODING, "gzip"),
                    Header.header(Headers.CONTENT_LENGTH, gzipped.size.toString())
                )
                .withBody(gzipped)
        )

        val (_, result, response) = Fuel.request(Method.POST, mock.path("gzip"))
                .header(Headers.ACCEPT_ENCODING, "gzip")
                .body(value)
                .response()

        assertArrayEquals(value, response.component1())
        assertThat(result[Headers.CONTENT_ENCODING].lastOrNull(), nullValue())
        assertThat(result[Headers.CONTENT_LENGTH].lastOrNull(), nullValue())
    }
}