package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.test.MockHttpTestCase
import com.google.common.net.MediaType
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.fail
import org.junit.Test
import org.mockserver.model.BinaryBody
import java.io.File
import java.util.Random
import java.util.concurrent.TimeUnit

class CancellableRequestTest : MockHttpTestCase() {

    @Test
    fun cancelDownloadRequest() {
        val bytes = ByteArray(1024 * 1024).apply { Random().nextBytes(this) }

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/bytes"),
            response = mock.response()
                .withBody(BinaryBody(bytes, MediaType.OCTET_STREAM))
                .withDelay(TimeUnit.SECONDS, 1)
        )

        val file = File.createTempFile(bytes.toString(), null)
        val requestPrimed = FuelManager()
            .download(mock.path("bytes"))
            .destination { _, _ -> file }

        var resolved = false
        val running = requestPrimed.response { _, _, _ -> resolved = true }

        running.cancel()
        running.join()

        assertThat(resolved, equalTo(false))
    }

    @Test
    fun cancelInterruptCallback() {
        val bytes = ByteArray(1024 * 1024).apply { Random().nextBytes(this) }

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/bytes"),
            response = mock.response().withBody(BinaryBody(bytes, MediaType.OCTET_STREAM))
        )

        var interrupted = false
        val file = File.createTempFile(bytes.toString(), null)

        val requestPrimed = FuelManager()
            .download(mock.path("bytes"))
            .destination { _, _ -> file }
            .interrupt { interrupted = true }

        val running = requestPrimed.response { _, _, result -> fail(result.toString()) }
        running.cancel()
        running.join()

        assertThat(interrupted, equalTo(true))
    }
}