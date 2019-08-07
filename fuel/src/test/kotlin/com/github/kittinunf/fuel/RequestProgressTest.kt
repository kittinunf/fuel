package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FileDataPart
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.test.MockHttpTestCase
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.net.HttpURLConnection

class RequestProgressTest : MockHttpTestCase() {
    private val currentDir: File by lazy {
        val dir = System.getProperty("user.dir")
        File(dir, "src/test/assets")
    }

    private val threadSafeFuel = FuelManager()

    @Test
    fun reportsRequestProgressWithUpload() {
        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/upload"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_ACCEPTED)
        )

        val file = File(currentDir, "lorem_ipsum_long.tmp")
        val length = file.length()
        var progressCalls = 0
        var expectedLength: Long

        val (request, response, result) = threadSafeFuel.upload(mock.path("upload"))
            .add(FileDataPart(file))
            .progress { _, _ -> progressCalls += 1 }
            .also { expectedLength = it.body.length!! }
            .also { println("Request body is $expectedLength bytes ($length bytes of file data)") }
            .responseString()

        assertFalse(request.executionOptions.requestProgress.isNotSet())

        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat("Expected data, actual error $error.", data, notNullValue())

        // Probably 3: 2 flushes to write, 1 after it completes
        assertThat("Expected progress to be called at least (total size/buffer size), actual $progressCalls calls",
            progressCalls > expectedLength / threadSafeFuel.progressBufferSize,
            equalTo(true)
        )
    }

    @Test
    fun reportsRequestProgressWithGenericPost() {
        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/upload"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_ACCEPTED)
        )

        val file = File(currentDir, "lorem_ipsum_long.tmp")
        val length = file.length()
        var progressCalls = 0

        val (request, response, result) = threadSafeFuel.request(Method.POST, mock.path("upload"))
            .body(file.also { println("Uploading $length bytes") })
            .requestProgress { _, _ -> progressCalls += 1 }
            .responseString()

        assertFalse(request.executionOptions.requestProgress.isNotSet())

        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat("Expected data, actual error $error.", data, notNullValue())

        // Probably 2, as the body is written as a whole (per buffer size)
        assertThat("Expected progress to be called at least (total size/buffer size), actual $progressCalls calls",
            progressCalls > length / threadSafeFuel.progressBufferSize,
            equalTo(true)
        )

        println(progressCalls)
    }

    @Test
    fun ensureProgressIsNotSetWhenNoCallbackIsGiven() {
        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/upload"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_ACCEPTED)
        )

        val file = File(currentDir, "lorem_ipsum_long.tmp")

        val (request, _, _) = threadSafeFuel.upload(mock.path("upload"))
            .add(FileDataPart(file))
            .responseString()

        assertTrue(request.executionOptions.requestProgress.isNotSet())
    }
}
