package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.test.MockHttpTestCase
import com.google.common.net.MediaType
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockserver.model.BinaryBody
import org.mockserver.model.Delay
import java.io.File
import java.util.Random

class ResponseProgressTest : MockHttpTestCase() {

    private val threadSafeFuel = FuelManager()

    @Test
    fun reportsResponseProgressWithDownload() {
        val length = threadSafeFuel.progressBufferSize * 8
        val file = File.createTempFile(length.toString(), null)
        val downloadData = ByteArray(length).apply { Random().nextBytes(this) }

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/download"),
            response = mock.response().withDelay(Delay.seconds(1)).withBody(BinaryBody(downloadData, MediaType.OCTET_STREAM))
        )

        var progressCalls = 0

        val (request, response, result) = threadSafeFuel.download(mock.path("download"))
            .destination { _, _ -> file.also { println("Downloading $length bytes to file") } }
            .progress { _, _ -> progressCalls += 1 }
            .responseString()

        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat("Expected data, actual error $error.", data, notNullValue())

        // Probably around 9 (8 times a buffer write, and the final closing -1 write)
        assertThat("Expected progress to be called at least (total size/buffer size), actual $progressCalls calls",
            progressCalls > length / threadSafeFuel.progressBufferSize,
            equalTo(true)
        )
    }

    @Test
    fun reportsResponseProgressWithGenericGet() {
        val length = threadSafeFuel.progressBufferSize * 8 - 200
        val downloadData = ByteArray(length).apply { Random().nextBytes(this) }

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/download"),
            response = mock.response().withDelay(Delay.seconds(1)).withBody(BinaryBody(downloadData, MediaType.OCTET_STREAM))
        )

        var progressCalls = 0

        val (request, response, result) = threadSafeFuel.request(Method.GET, mock.path("download"))
            .responseProgress { _, _ -> progressCalls += 1 }
            .also { println("Downloading $length bytes to memory") }
            .responseString()

        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat("Expected data, actual error $error.", data, notNullValue())

        // Probably around 9 (8 times a buffer write, and the final closing -1 write)
        assertThat("Expected progress to be called at least (total size/buffer size), actual $progressCalls calls",
            progressCalls > length / threadSafeFuel.progressBufferSize,
            equalTo(true)
        )
    }
}
