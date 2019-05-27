package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Handler
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.test.MockHttpTestCase
import com.google.common.net.MediaType
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.fail
import org.junit.Test
import org.mockserver.model.BinaryBody
import java.io.ByteArrayInputStream
import java.io.File
import java.util.Random
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

class CancellableRequestTest : MockHttpTestCase() {
    private fun expectNoResponseCallbackHandler() = object : Handler<ByteArray> {
        override fun success(value: ByteArray) { fail("Expected to not hit success path, actual $value") }
        override fun failure(error: FuelError) { fail("Expected to not hit failure path, actual $error") }
    }

    @Test
    fun testCancellationDuringSendingRequest() {
        val semaphore = Semaphore(0)

        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/cancel-during-request"),
            response = mock.reflect().withDelay(TimeUnit.MILLISECONDS, 200)
        )

        val request = Fuel.post(mock.path("cancel-during-request"))
        val running = request
            .requestProgress { _, _ -> request.tryCancel() }
            .body("my-body")
            .interrupt { semaphore.acquire() }
            .response(expectNoResponseCallbackHandler())
        
        assertsThat(1, semaphore.availablePermits())
        
        semaphore.release()

        assertThat("Expected request to be cancelled via interruption $running", semaphore.tryAcquire())

        assertThat("Fuel isDone true", running.isDone, equalTo(true))
        assertThat("Fuel isCancelled true", running.isCancelled, equalTo(true))
    }

    @Test
    fun testCancellationDuringReceivingResponse() {
        val manager = FuelManager()
        val interruptedSemaphore = Semaphore(0)
        val responseWrittenSemaphore = Semaphore(0)
        val bytes = ByteArray(10 * manager.progressBufferSize).apply { Random().nextBytes(this) }
        val file = File.createTempFile("random-bytes", ".bin")

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/cancel-response"),
            response = mock.response().withBody(bytes).withDelay(TimeUnit.MILLISECONDS, 200)
        )

        val running = manager.download(mock.path("cancel-response"))
            .fileDestination { _, _ -> file }
            .responseProgress { readBytes, _ ->
                responseWrittenSemaphore.release()
                Thread.sleep(200)

                if (readBytes > 9 * manager.progressBufferSize)
                    fail("Expected request to be cancelled by now")
            }
            .interrupt { interruptedSemaphore.acquire() }
            .response(expectNoResponseCallbackHandler())

        assertThat("Expected body to be at least ${3 * manager.progressBufferSize} bytes",
            responseWrittenSemaphore.tryAcquire(3, 5, TimeUnit.SECONDS)
        )

        // Cancel while writing body
        running.cancel()
        
        assertThat(1, interruptedSemaphore.availablePermits())
        
        interruptedSemaphore.release()

        // Run the request
        assertThat("Expected request to be cancelled via interruption",
            interruptedSemaphore.tryAcquire(5, TimeUnit.SECONDS)
        )

        assertThat("FuelManager isDone true", running.isDone, equalTo(true))
        assertThat("FuelManager isCancelled true", running.isCancelled, equalTo(true))
        assertThat("Expected file to be incomplete", file.length() < bytes.size, equalTo(true))
    }

    @Test
    fun testCancellationInline() {
        val interruptSemaphore = Semaphore(0)
        val bodyReadSemaphore = Semaphore(0)

        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/cancel-inline"),
            response = mock.reflect().withDelay(TimeUnit.MILLISECONDS, 200)
        )

        val running = FuelManager()
            .request(Method.POST, mock.path("cancel-inline"), listOf("foo" to "bar"))
            .authentication().basic("username", "password")
            .body(
                { ByteArrayInputStream("my-body".toByteArray()).also { bodyReadSemaphore.release() } },
                { "my-body".length.toLong() }
            )
            .interrupt { iinterruptSemaphore.acquire() }
            .response(expectNoResponseCallbackHandler())
       
        assertThat("Expected body to be serialized", bodyReadSemaphore.tryAcquire(5, TimeUnit.SECONDS))

        running.cancel()
        
        assertThat(1, interruptSemaphore.availablePermits())
        
        interruptSemaphore.release()

        assertThat("Expected request to be cancelled via interruption $running",
            interruptSemaphore.tryAcquire()
        )

        assertThat("FuelManager isDone true", running.isDone, equalTo(true))
        assertThat("FuelManager isCancelled true", running.isCancelled, equalTo(true))
    }

    @Test
    fun interruptCallback() {
        val manager = FuelManager()
        val interruptSemaphore = Semaphore(0)
        val responseWrittenCallback = Semaphore(0)
        val bytes = ByteArray(10 * manager.progressBufferSize).apply { Random().nextBytes(this) }

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/bytes"),
            response = mock.response()
                .withBody(BinaryBody(bytes, MediaType.OCTET_STREAM))
                .withDelay(TimeUnit.MILLISECONDS, 200)
        )

        val file = File.createTempFile(bytes.toString(), null)

        val running = FuelManager()
            .download(mock.path("bytes"))
            .fileDestination { _, _ -> file }
            .header(Headers.CONTENT_TYPE, "application/octet-stream")
            .responseProgress { _, _ ->
                responseWrittenCallback.release()
                Thread.sleep(200)
            }
            .interrupt { interruptSemaphore.acquire() }
            .response(expectNoResponseCallbackHandler())

        assertThat("Expected response to be partially written",
            responseWrittenCallback.tryAcquire(5, TimeUnit.SECONDS)
        )

        running.cancel()
        
        assertThat(1, interruptSemaphore.availablePermits())
        
        interruptSemaphore.release()

        assertThat("Expected request to be cancelled via interruption",
            interruptSemaphore.tryAcquire(5, TimeUnit.SECONDS)
        )

        assertThat("FuelManager isDone true", running.isDone, equalTo(true))
        assertThat("FuelManager isCancelled true", running.isCancelled, equalTo(true))
    }
}
