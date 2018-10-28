package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.Blob
import com.github.kittinunf.fuel.core.DataPart
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.MultipartBody.Companion.retrieveBoundaryInfo
import com.github.kittinunf.fuel.core.RegularRequest
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.isA
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException
import java.net.HttpURLConnection
import java.net.URL
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RequestUploadTest : MockHttpTestCase() {
    private val currentDir: File by lazy {
        val dir = System.getProperty("user.dir")
        File(dir, "src/test/assets")
    }

    @Test
    fun httpUploadWithPostCase() {
        val manager = FuelManager()

        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/upload"),
            response = mock.reflect()
        )

        val (request, response, result) = manager.upload(mock.path("upload")).source("file-name") {
            _, _ -> File(currentDir, "lorem_ipsum_short.tmp")
        }.responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpUploadWithPostAndParamsCase() {
        val manager = FuelManager()

        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/upload"),
            response = mock.reflect()
        )

        val (request, response, result) = manager.upload(mock.path("upload"), param = listOf("foo" to "bar"))
                .source { _, _ ->
                    File(currentDir, "lorem_ipsum_short.tmp")
                }
                .responseString()

        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpUploadWithPutCase() {
        val manager = FuelManager()

        mock.chain(
            request = mock.request().withMethod(Method.PUT.value).withPath("/upload"),
            response = mock.reflect()
        )

        val (request, response, result) = manager.upload(mock.path("upload"), Method.PUT)
            .source { _, _ -> File(currentDir, "lorem_ipsum_long.tmp") }
            .responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpUploadWithProgressValidCase() {
        val manager = FuelManager()

        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/upload"),
            response = mock.reflect()
        )

        var read = -1L
        var total = -1L

        val (request, response, result) = manager.upload(mock.path("upload")).source { _, _ ->
                    File(currentDir, "lorem_ipsum_long.tmp")
                }.progress { readBytes, totalBytes ->
                    read = readBytes
                    total = totalBytes
                    println("read: $read, total: $total")
                }.responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        assertThat(read == total && read != -1L && total != -1L, isEqualTo(true))
    }

    @Test
    fun httpUploadWithProgressInvalidEndPointCase() {
        val manager = FuelManager()

        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/nope"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        val (request, response, result) = manager.upload(mock.path("nope")).source { _, _ ->
                    File(currentDir, "lorem_ipsum_short.tmp")
                }.progress { _, _ ->
                }.responseString()

        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, notNullValue())
        assertThat(data, nullValue())

        val statusCode = HttpURLConnection.HTTP_NOT_FOUND
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpUploadWithProgressInvalidFileCase() {
        val manager = FuelManager()

        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/upload"),
            response = mock.reflect()
        )

        val (request, response, result) = manager.upload(mock.path("upload")).source { _, _ ->
                    File(currentDir, "not_found_file.tmp")
                }.progress { _, _ ->
                }.responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, notNullValue())
        assertThat(data, nullValue())

        assertThat(error?.exception as FileNotFoundException, isA(FileNotFoundException::class.java))

        val statusCode = -1
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpUploadWithMultipleFiles() {
        val manager = FuelManager()

        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/upload"),
            response = mock.reflect()
        )

        val (request, response, result) = manager.upload(mock.path("upload"), param = listOf("foo" to "bar"))
                .source("file-name_1") { _, _ -> File(currentDir, "lorem_ipsum_short.tmp") }
                .source("file-name_2") { _, _ -> File(currentDir, "lorem_ipsum_long.tmp") }
                .responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val string = data as String
        assertThat(string, containsString("file-name_1"))
        assertThat(string, containsString("file-name_2"))

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpUploadWithMultipleDataParts() {
        val manager = FuelManager()

        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/upload"),
            response = mock.reflect()
        )

        val (request, response, result) = manager.upload(mock.path("upload"), param = listOf("foo" to "bar"))
                .dataPart(
                    DataPart.from(File(currentDir, "lorem_ipsum_short.tmp"), contentType = "image/jpeg"),
                    DataPart.from(File(currentDir, "lorem_ipsum_long.tmp"), fileName = "second-file", contentType = "image/jpeg")
                )
                .responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val string = data as String
        assertThat(string, containsString("lorem_ipsum_short"))
        assertThat(string, containsString("second-file"))

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpUploadWithNamedBlob() {
        val file = File(currentDir, "lorem_ipsum_short.tmp")
        val manager = FuelManager()

        mock.chain(
                request = mock.request().withMethod(Method.POST.value).withPath("/upload"),
                response = mock.reflect()
        )

        val (request, response, result) = manager.upload(mock.path("upload"), param = listOf("foo" to "bar"))
                .dataPart(
                    DataPart.from(
                        Blob(inputStream = { file.inputStream() }, length = file.length(), name = file.name),
                        fileName = "coolblob"
                    )
                )
                .responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val string = data as String
        assertThat(string, containsString("coolblob"))

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpUploadWithSpecifiedBoundary() {
        val manager = FuelManager()

        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/upload"),
            response = mock.reflect()
        )

        val (request, response, result) = manager.upload(mock.path("upload"), param = listOf("foo" to "bar"))
                .multipart()
                .source { _, _ -> File(currentDir, "lorem_ipsum_short.tmp") }
                .header(Headers.CONTENT_TYPE to "multipart/form-data; boundary=160f77ec3eff")
                .responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val string = data as String
        assertThat(string, containsString("boundary=160f77ec3eff"))

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpUploadWithEmptyBoundary() {
        val manager = FuelManager()

        mock.chain(
                request = mock.request().withMethod(Method.POST.value).withPath("/upload"),
                response = mock.reflect()
        )

        val (request, response, result) = manager.upload(mock.path("upload"), param = listOf("foo" to "bar"))
                .source { _, _ ->
                    File(currentDir, "lorem_ipsum_short.tmp")
                }
                .responseString()

        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val string = data as String
        assertThat(string, containsString("boundary="))

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun getBoundaryWithBoundaryHeaders() {
        val request = RegularRequest(Method.POST, "", URL("http://httpbin.org"))
        request.header(Pair(Headers.CONTENT_TYPE, "multipart/form-data; boundary=160f77ec3eff"))

        val boundary = retrieveBoundaryInfo(request)

        assertThat(boundary, equalTo("160f77ec3eff"))
    }

    @Test
    fun getBoundaryWithEmptyHeaders() {
        val request = RegularRequest(Method.POST, "", URL("http://httpbin.org"))
        val boundary = retrieveBoundaryInfo(request)

        assertThat(boundary, notNullValue())
    }
}
