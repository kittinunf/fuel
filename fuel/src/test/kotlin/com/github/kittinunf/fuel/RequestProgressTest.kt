package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.Method
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.io.File
import java.net.HttpURLConnection

class RequestProgressTest : MockHttpTestCase() {
    private val currentDir: File by lazy {
        val dir = System.getProperty("user.dir")
        File(dir, "src/test/assets")
    }

    @Test
    fun reportsRequestProgressWithUpload() {
        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/upload"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_ACCEPTED)
        )

        val file = File(currentDir, "lorem_ipsum_long.tmp")
        val length = file.length()
        var progressCalls = 0

        val (request, response, result) = Fuel.upload(mock.path("upload"))
            .source { _, _ -> file.also { println("Uploading $length bytes") } }
            .progress { _, _ -> progressCalls += 1 }
            .responseString()

        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat("Expected data, actual error $error.", data, notNullValue())

        // Probably around 12: 1 buffer flush of the file, and a few calls per data part
        assertThat("Expected progress to be called at least (total size/buffer size), actual $progressCalls calls",
            progressCalls > 1,
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

        val (request, response, result) = Fuel.post(mock.path("upload"))
            .body(file.also { println("Uploading $length bytes") })
            .requestProgress { _, _ -> progressCalls += 1 }
            .responseString()

        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat("Expected data, actual error $error.", data, notNullValue())

        // Probably 2, as the body is written as a whole (per buffer size)
        assertThat("Expected progress to be called at least (total size/buffer size), actual $progressCalls calls",
            progressCalls > 1,
            equalTo(true)
        )
    }
}
