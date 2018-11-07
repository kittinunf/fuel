package com.github.kittinunf.fuel.toolbox

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.test.MockHttpTestCase
import com.github.kittinunf.fuel.util.encode
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import org.mockserver.model.Header
import java.io.ByteArrayOutputStream

class TransferEncodingTest : MockHttpTestCase() {

    @Test
    fun identityTransferEncodingTest() {
        val value = ByteArray(32).apply {
            for (i in 0..(this.size - 1)) {
                this[i] = ('A'..'z').random().toByte()
            }
        }

        val identity = ByteArrayOutputStream(value.size).let {
            it.write(value)
            it.toByteArray()
        }

        mock.chain(
            request = mock.request()
                .withMethod(Method.POST.value)
                .withPath("/identity")
                .withHeader(Header.header(Headers.ACCEPT_TRANSFER_ENCODING, "gzip, deflate; q=0.5")),
            response = mock.response()
                .withHeaders(
                    Header.header(Headers.TRANSFER_ENCODING, "identity"),
                    Header.header(Headers.CONTENT_LENGTH, value.size.toString())
                )
                .withBody(identity)
        )

        val (_, result, response) = Fuel.request(Method.POST, mock.path("identity"))
            .body(value)
            .response()

        assertArrayEquals(value, response.component1())
        assertThat(result[Headers.CONTENT_ENCODING].lastOrNull(), nullValue())
        assertThat(result[Headers.CONTENT_LENGTH].lastOrNull(), equalTo(value.size.toString()))
    }

    @Test
    fun gzipTransferEncodingTest() {
        val value = ByteArray(32).apply {
            for (i in 0..(this.size - 1)) {
                this[i] = ('A'..'z').random().toByte()
            }
        }

        val inner = ByteArrayOutputStream(value.size)
        inner.encode("gzip").apply {
            write(value)
            close()
        }

        // It's written to here
        val gzipped = inner.toByteArray()

        mock.chain(
            request = mock.request()
                .withMethod(Method.POST.value)
                .withPath("/gzip")
                .withHeader(Header.header(Headers.ACCEPT_TRANSFER_ENCODING, "gzip, deflate; q=0.5")),
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

        val (data, error) = response
        assertThat("Expected data, actual error $error", data, notNullValue())
        assertArrayEquals(value, data)
        assertThat(result[Headers.CONTENT_ENCODING].lastOrNull(), nullValue())
        assertThat(result[Headers.CONTENT_LENGTH].lastOrNull(), nullValue())
    }

    @Test
    fun deflateTransferEncodingTest() {
        val value = ByteArray(32).apply {
            for (i in 0..(this.size - 1)) {
                this[i] = ('A'..'z').random().toByte()
            }
        }

        val inner = ByteArrayOutputStream(value.size)
        inner.encode("deflate").apply {
            write(value)
            close()
        }

        // It's written to here
        val gzipped = inner.toByteArray()

        mock.chain(
            request = mock.request()
                .withMethod(Method.POST.value)
                .withPath("/deflate")
                .withHeader(Header.header(Headers.ACCEPT_TRANSFER_ENCODING, "gzip, deflate; q=0.5")),
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


        val (data, error) = response
        assertThat("Expected data, actual error $error", data, notNullValue())
        assertArrayEquals(value, data)
        assertThat(result[Headers.CONTENT_ENCODING].lastOrNull(), nullValue())
        assertThat(result[Headers.CONTENT_LENGTH].lastOrNull(), nullValue())
    }

    @Test
    fun stackedTransferEncodingTest() {
        val value = ByteArray(32).apply {
            for (i in 0..(this.size - 1)) {
                this[i] = ('A'..'z').random().toByte()
            }
        }

        val innerData = ByteArrayOutputStream(value.size * 2)
        innerData.encode("gzip").apply {
            write(value)
            close()
        }

        val outputData = ByteArrayOutputStream(value.size * 2)
        outputData.encode("gzip").apply {
            write(innerData.toByteArray())
            close()
        }

        // It's written to here
        val gzipped = outputData.toByteArray()

        mock.chain(
            request = mock.request()
                .withMethod(Method.POST.value)
                .withPath("/stacked")
                .withHeader(Header.header(Headers.ACCEPT_TRANSFER_ENCODING, "gzip, deflate; q=0.5")),
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

        val (data, error) = response
        assertThat("Expected data, actual error $error", data, notNullValue())
        assertArrayEquals(value, data)
        assertThat(result[Headers.CONTENT_ENCODING].lastOrNull(), nullValue())
        assertThat(result[Headers.CONTENT_LENGTH].lastOrNull(), nullValue())
    }
}