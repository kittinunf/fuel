package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.ResponseResultOf
import com.github.kittinunf.fuel.test.MockHttpTestCase
import com.google.common.net.MediaType
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.isA
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Test
import org.mockserver.model.BinaryBody
import org.mockserver.model.Delay
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.util.Random
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class DownloadRequestTest : MockHttpTestCase() {

    private fun <T: Any> assertDownloadedBytesToFile(result: ResponseResultOf<T>, file: File, numberOfBytes: Int): ResponseResultOf<T> {
        val (request, response, wrapped) = result
        val (data, error) = wrapped

        assertThat("Expected request to not be null", request, notNullValue())
        assertThat("Expected response to not be null", response, notNullValue())
        assertThat("Expected data, actual $error", data, notNullValue())
        assertThat("Expected file length ${file.length()} to match $numberOfBytes", file.length(), equalTo(numberOfBytes.toLong()))
        assertThat(response.statusCode, equalTo(HttpURLConnection.HTTP_OK))

        return result
    }

    @Test
    fun downloadToFile() {
        val manager = FuelManager()

        val numberOfBytes = 32768
        val file = File.createTempFile(numberOfBytes.toString(), null)
        val bytes = ByteArray(numberOfBytes).also { Random().nextBytes(it) }

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/bytes"),
            response = mock.response().withBody(BinaryBody(bytes, MediaType.OCTET_STREAM))
        )

        val result = manager.download(mock.path("bytes"))
            .fileDestination { _, _ -> file }
            .response()

        assertDownloadedBytesToFile(result, file, numberOfBytes)
    }

    @Test
    fun downloadToStream() {
        val manager = FuelManager()

        val numberOfBytes = 32768
        val stream = ByteArrayOutputStream(numberOfBytes)
        val bytes = ByteArray(numberOfBytes).also { Random().nextBytes(it) }

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/bytes"),
            response = mock.response().withBody(BinaryBody(bytes, MediaType.OCTET_STREAM))
        )

        val result = manager.download(mock.path("bytes"))
            .streamDestination { _, _ -> Pair(stream, { ByteArrayInputStream(stream.toByteArray()) }) }
            .response()

        val (request, response, wrapped) = result
        val (data, error) = wrapped

        assertThat("Expected request to not be null", request, notNullValue())
        assertThat("Expected response to not be null", response, notNullValue())
        assertThat("Expected data, actual $error", data, notNullValue())
        assertThat("Expected stream output length ${stream.size()} to match $numberOfBytes", stream.size(), equalTo(numberOfBytes))
        assertThat(response.statusCode, equalTo(HttpURLConnection.HTTP_OK))
    }

    @Test
    fun downloadBytesWithProgress() {
        val manager = FuelManager()

        val numberOfBytes = 1186
        val file = File.createTempFile(numberOfBytes.toString(), null)
        val bytes = ByteArray(numberOfBytes).also { Random().nextBytes(it) }

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/bytes"),
            response = mock.response().withBody(BinaryBody(bytes, MediaType.OCTET_STREAM))
        )

        var read = -1L
        var total = -1L

        val triple = manager.download(mock.path("bytes"))
            .fileDestination { _, _ -> file }
            .progress { readBytes, totalBytes -> read = readBytes; total = totalBytes }
            .response()

        val (_, _, result) = assertDownloadedBytesToFile(triple, file, numberOfBytes)
        val (data, _) = result

        assertThat(data, isA(ByteArray::class.java))
        assertThat(data!!.size.toLong(), equalTo(read))
        assertThat("Progress read bytes and total bytes should be equal",
            read == total && read != -1L && total != -1L,
            equalTo(true)
        )
    }

    @Test
    fun downloadStringWithProgress() {
        val manager = FuelManager()

        val numberOfBytes = DEFAULT_BUFFER_SIZE * 5
        val file = File.createTempFile(numberOfBytes.toString(), null)
        val bytes = ByteArray(numberOfBytes).also { Random().nextBytes(it) }

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/bytes"),
            response = mock.response().withBody(BinaryBody(bytes, MediaType.OCTET_STREAM))
        )

        var read = -1L
        var total = -1L

        val triple = manager.download(mock.path("bytes"))
            .fileDestination { _, _ -> file }
            .progress { readBytes, totalBytes -> read = readBytes; total = totalBytes }
            .responseString()

        val (_, _, result) = assertDownloadedBytesToFile(triple, file, numberOfBytes)
        val (data, _) = result

        assertThat(data, isA(String::class.java))
        assertThat(data, equalTo(file.readText()))
        assertThat(
            "Progress read bytes and total bytes should be equal",
            read == total && read != -1L && total != -1L,
            equalTo(true)
        )
    }

    @Test
    fun downloadFromHttpNotFound() {
        val manager = FuelManager()

        val numberOfBytes = 131072
        val file = File.createTempFile(numberOfBytes.toString(), null)

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/bytes"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        val (request, response, result) = manager.download(mock.path("bytes"))
            .fileDestination { _, _ -> file }
            .progress { _, _ -> }
            .responseString()
        val (data, error) = result

        assertThat("Expected request to not be null", request, notNullValue())
        assertThat("Expected response to not be null", response, notNullValue())
        assertThat("Expected error, actual $data", error, notNullValue())
        assertThat("Expected nothing to be written to file", file.length(), equalTo(0L))

        val statusCode = HttpURLConnection.HTTP_NOT_FOUND
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun downloadToInvalidFileDestination() {
        val manager = FuelManager()

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/bytes"),
            response = mock.reflect()
        )

        val (request, response, result) = manager.download(mock.path("bytes"))
            .fileDestination { _, _ ->
                val dir = System.getProperty("user.dir")
                File.createTempFile("not_found_file", null, File(dir, "not-a-folder"))
            }
            .responseString()
        val (data, error) = result

        assertThat("Expected request to not be null", request, notNullValue())
        assertThat("Expected response to not be null", response, notNullValue())
        assertThat("Expected error, actual $data", error, notNullValue())

        val statusCode = 200
        assertThat(error?.exception as IOException, isA(IOException::class.java))
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun downloadBigFile() {
        val manager = FuelManager()

        val numberOfBytes = 1024 * 1024 * 10 // 10 MB
        val file = File.createTempFile(numberOfBytes.toString(), null)
        val bytes = ByteArray(numberOfBytes).apply { Random().nextBytes(this) }

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/bytes"),
            response = mock.response().withDelay(Delay.seconds(1)).withBody(BinaryBody(bytes, MediaType.OCTET_STREAM))
        )

        var read = -1L
        var total = -1L

        val triple = manager.download(mock.path("bytes"))
            .fileDestination { _, _ -> file }
            .progress { readBytes, totalBytes -> read = readBytes; total = totalBytes }
            .response()

        assertDownloadedBytesToFile(triple, file, numberOfBytes)
        assertThat("Progress read bytes and total bytes should be equal",
                read == total && read != -1L && total != -1L,
                equalTo(true)
        )
    }
}
