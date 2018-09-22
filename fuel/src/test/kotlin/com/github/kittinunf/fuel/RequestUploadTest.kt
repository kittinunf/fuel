package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.Blob
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.DataPart
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.requests.retrieveBoundaryInfo
import org.hamcrest.CoreMatchers.*
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

        mockChain(
            request = mockRequest().withMethod(Method.POST.value).withPath("/upload"),
            response = mockReflect()
        )

        val (request, response, result) = manager.upload(mockPath("upload")).source { _, _ ->
                    File(currentDir, "lorem_ipsum_short.tmp")
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

        mockChain(
            request = mockRequest().withMethod(Method.POST.value).withPath("/upload"),
            response = mockReflect()
        )

        val (request, response, result) = manager.upload(mockPath("upload"), param = listOf("foo" to "bar"))
                .source { _, _ ->
                    File(currentDir, "lorem_ipsum_short.tmp")
                }
                .name { "file-name" }
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

        mockChain(
            request = mockRequest().withMethod(Method.PUT.value).withPath("/upload"),
            response = mockReflect()
        )

        val (request, response, result) = manager.upload(mockPath("upload"), Method.PUT)
                .source { _, _ -> File(currentDir, "lorem_ipsum_long.tmp")
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
    fun httpUploadWithProgressValidCase() {
        val manager = FuelManager()

        mockChain(
            request = mockRequest().withMethod(Method.POST.value).withPath("/upload"),
            response = mockReflect()
        )

        var read = -1L
        var total = -1L

        val (request, response, result) = manager.upload(mockPath("upload")).source { _, _ ->
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

        mockChain(
            request = mockRequest().withMethod(Method.POST.value).withPath("/nope"),
            response = mockResponse().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        val (request, response, result) = manager.upload(mockPath("nope")).source { _, _ ->
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

        mockChain(
            request = mockRequest().withMethod(Method.POST.value).withPath("/upload"),
            response = mockReflect()
        )

        val (request, response, result) = manager.upload(mockPath("upload")).source { _, _ ->
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

        mockChain(
            request = mockRequest().withMethod(Method.POST.value).withPath("/upload"),
            response = mockReflect()
        )

        val (request, response, result) = manager.upload(mockPath("upload"), param = listOf("foo" to "bar"))
                .sources { _, _ ->
                    listOf(File(currentDir, "lorem_ipsum_short.tmp"),
                            File(currentDir, "lorem_ipsum_long.tmp"))
                }
                .name { "file-name" }
                .responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val string = data as String
        assertThat(string, containsString("file-name1"))
        assertThat(string, containsString("file-name2"))

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpUploadWithMultipleDataParts() {
        val manager = FuelManager()

        mockChain(
            request = mockRequest().withMethod(Method.POST.value).withPath("/upload"),
            response = mockReflect()
        )

        val (request, response, result) = manager.upload(mockPath("upload"), param = listOf("foo" to "bar"))
                .dataParts { _, _ ->
                    listOf(
                            DataPart(File(currentDir, "lorem_ipsum_short.tmp"), type = "image/jpeg"),
                            DataPart(File(currentDir, "lorem_ipsum_long.tmp"), name = "second-file", type = "image/jpeg")
                    )
                }
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

        mockChain(
                request = mockRequest().withMethod(Method.POST.value).withPath("/upload"),
                response = mockReflect()
        )

        val (request, response, result) = manager.upload(mockPath("upload"), param = listOf("foo" to "bar"))
                .blob { r, _ ->
                    r.name = "coolblob"
                    Blob(inputStream = { file.inputStream() }, length = file.length(), name = file.name)
                }
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

        mockChain(
                request = mockRequest().withMethod(Method.POST.value).withPath("/upload"),
                response = mockReflect()
        )

        val (request, response, result) = manager.upload(mockPath("upload"), param = listOf("foo" to "bar"))
                .source { r, _ ->
                    r.header(Pair("Content-Type", "multipart/form-data; boundary=160f77ec3eff"))
                    File(currentDir, "lorem_ipsum_short.tmp")
                }
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

        mockChain(
                request = mockRequest().withMethod(Method.POST.value).withPath("/upload"),
                response = mockReflect()
        )

        val (request, response, result) = manager.upload(mockPath("upload"), param = listOf("foo" to "bar"))
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
        val request = Request(Method.POST, "", URL("http://httpbin.org"),
                timeoutInMillisecond = 15000,
                timeoutReadInMillisecond = 15000)
        request.header(Pair("Content-Type", "multipart/form-data; boundary=160f77ec3eff"))

        val boundary = retrieveBoundaryInfo(request)

        assertThat(boundary, equalTo("160f77ec3eff"))
    }

    @Test
    fun getBoundaryWithEmptyHeaders() {
        val request = Request(Method.POST, "", URL("http://httpbin.org"),
                timeoutInMillisecond = 15000,
                timeoutReadInMillisecond = 15000)
        val boundary = retrieveBoundaryInfo(request)

        assertThat(boundary, notNullValue())
    }
}
