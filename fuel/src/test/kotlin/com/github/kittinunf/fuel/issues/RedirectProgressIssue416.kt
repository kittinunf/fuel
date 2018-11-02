package com.github.kittinunf.fuel.issues

import com.github.kittinunf.fuel.MockHttpTestCase
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import com.google.common.net.MediaType
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockserver.model.BinaryBody
import java.io.File
import java.net.HttpURLConnection
import java.util.Random

class RedirectProgressIssue416 : MockHttpTestCase() {

    private val threadSafeFuel = FuelManager()

    @Test
    fun itCorrectlyReportsProgressAfterRedirection() {
        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/download-redirect"),
            response = mock.response()
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)
                .withHeader(Headers.LOCATION, "/redirected")
        )

        val numberOfBytes = threadSafeFuel.progressBufferSize * 8
        val file = File.createTempFile(numberOfBytes.toString(), null)
        val bytes = ByteArray(numberOfBytes).apply { Random().nextBytes(this) }

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/redirected"),
            response = mock.response().withBody(BinaryBody(bytes, MediaType.OCTET_STREAM))
        )

        var progressCalled = 0L
        val request = threadSafeFuel.download(mock.path("download-redirect"))
            .destination { _, _ -> file }
            .progress { _, _ -> progressCalled += 1 }

        val (_, _, result) = request.response()
        val (data, error) = result

        assertThat("Expected data, actual error $error", data, notNullValue())
        assertThat(data!!.size, equalTo(numberOfBytes))
        assertThat(progressCalled > (numberOfBytes / threadSafeFuel.progressBufferSize), equalTo(true))
    }
}
