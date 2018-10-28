package com.github.kittinunf.fuel.toolbox

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.MockHttpTestCase
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import org.mockserver.model.Header
import java.io.ByteArrayOutputStream
import java.util.zip.DeflaterOutputStream
import java.util.zip.GZIPOutputStream

class TransferEncodingTest : MockHttpTestCase() {
    @Test
    fun gzipTransferEncodingTest() {
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
                .withHeader(Header.header(Headers.ACCEPT_TRANSFER_ENCODING, HttpClient.SUPPORTED_DECODING.joinToString(", "))),
            response = mock.response()
                .withHeaders(
                    Header.header(Headers.TRANSFER_ENCODING, "gzip"),
                    Header.header(Headers.CONTENT_LENGTH, value.size.toString())
                )
                .withBody(gzipped)
        )

        val (_, result, response) = Fuel.request(Method.POST, mock.path("gzip"))
            .body(value)
            .response()

        assertArrayEquals(value, response.component1())
        assertThat(result[Headers.CONTENT_ENCODING].lastOrNull(), nullValue())
        assertThat(result[Headers.CONTENT_LENGTH].lastOrNull(), equalTo(value.size.toString()))
    }

    @Test
    fun deflateTransferEncodingTest() {
        val value = ByteArray(32).apply {
            for (i in 0..(this.size - 1)) {
                this[i] = ('A'..'z').random().toByte()
            }
        }

        val inner = ByteArrayOutputStream(value.size)
        val output = DeflaterOutputStream(inner)
        output.write(value)
        output.finish()

        // It's written to here
        val gzipped = inner.toByteArray()

        mock.chain(
            request = mock.request()
                .withMethod(Method.POST.value)
                .withPath("/deflate")
                .withHeader(Header.header(Headers.ACCEPT_TRANSFER_ENCODING, HttpClient.SUPPORTED_DECODING.joinToString(", "))),
            response = mock.response()
                .withHeaders(
                    Header.header(Headers.TRANSFER_ENCODING, "deflate"),
                    Header.header(Headers.CONTENT_LENGTH, value.size.toString())
                )
                .withBody(gzipped)
        )

        val (_, result, response) = Fuel.request(Method.POST, mock.path("deflate"))
            .body(value)
            .response()

        assertArrayEquals(value, response.component1())
        assertThat(result[Headers.CONTENT_ENCODING].lastOrNull(), nullValue())
        assertThat(result[Headers.CONTENT_LENGTH].lastOrNull(), equalTo(value.size.toString()))
    }

    @Test
    fun stackedTransferEncodingTest() {
        val value = ByteArray(32).apply {
            for (i in 0..(this.size - 1)) {
                this[i] = ('A'..'z').random().toByte()
            }
        }

        val innerData = ByteArrayOutputStream(value.size * 2)
        val inner = GZIPOutputStream(innerData)
        inner.write(value)
        inner.finish()

        val outputData = ByteArrayOutputStream(value.size * 2)
        val output = GZIPOutputStream(outputData)
        output.write(innerData.toByteArray())
        output.finish()

        // It's written to here
        val gzipped = outputData.toByteArray()
        println(String(gzipped))

        mock.chain(
            request = mock.request()
                .withMethod(Method.POST.value)
                .withPath("/stacked")
                .withHeader(Header.header(Headers.ACCEPT_TRANSFER_ENCODING, HttpClient.SUPPORTED_DECODING.joinToString(", "))),
            response = mock.response()
                .withHeaders(
                    Header.header(Headers.TRANSFER_ENCODING, "gzip, gzip"),
                    Header.header(Headers.CONTENT_LENGTH, value.size.toString())
                )
                .withBody(gzipped)
        )

        val (_, result, response) = Fuel.request(Method.POST, mock.path("stacked"))
            .body(value)
            .response()

        assertArrayEquals(value, response.component1())
        assertThat(result[Headers.CONTENT_ENCODING].lastOrNull(), nullValue())
        assertThat(result[Headers.CONTENT_LENGTH].lastOrNull(), equalTo(value.size.toString()))
    }
}