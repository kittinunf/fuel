package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.*
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException
import java.net.HttpURLConnection
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RequestUploadTest : BaseTestCase() {
    private val manager: FuelManager by lazy {
        FuelManager().apply {
            basePath = "http://httpbin.org"
        }
    }

    private val currentDir: File by lazy {
        val dir = System.getProperty("user.dir")
        File(dir, "src/test/assets")
    }

    @Test
    fun httpUploadWithPostCase() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.upload("/post").source { _, _ ->
            File(currentDir, "lorem_ipsum_short.tmp")
        }.responseString { req, res, result ->
            request = req
            response = res
            val (d, err) = result
            data = d
            error = err
        }

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpUploadWithPostAndParamsCase() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.upload("/post", param = listOf("foo" to "bar"))
                .source { _, _ ->
                    File(currentDir, "lorem_ipsum_short.tmp")
                }
                .name { "file-name" }
                .responseString { req, res, result ->
                    request = req
                    response = res
                    val (d, err) = result
                    data = d
                    error = err
                    print(d)
                }

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpUploadWithPutCase() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.upload("/put", Method.PUT).source { _, _ ->
            File(currentDir, "lorem_ipsum_long.tmp")
        }.responseString { req, res, result ->
            request = req
            response = res
            val (d, err) = result
            data = d
            error = err
        }

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpUploadWithProgressValidCase() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        var read = -1L
        var total = -1L

        manager.upload("/post").source { _, _ ->
            File(currentDir, "lorem_ipsum_long.tmp")
        }.progress { readBytes, totalBytes ->
            read = readBytes
            total = totalBytes
            println("read: $read, total: $total")
        }.responseString { req, res, result ->
            request = req
            response = res
            val (d, err) = result
            data = d
            error = err
        }

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))

        assertThat(read == total && read != -1L && total != -1L, isEqualTo(true))
    }

    @Test
    fun httpUploadWithProgressInvalidEndPointCase() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.upload("/pos").source { _, _ ->
            File(currentDir, "lorem_ipsum_short.tmp")
        }.progress { _, _ ->

        }.responseString { req, res, result ->
            request = req
            response = res
            val (d, err) = result
            data = d
            error = err
        }

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, notNullValue())
        assertThat(data, nullValue())

        val statusCode = HttpURLConnection.HTTP_NOT_FOUND
        assertThat(response?.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpUploadWithProgressInvalidFileCase() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.upload("/post").source { _, _ ->
            File(currentDir, "not_found_file.tmp")
        }.progress { _, _ ->

        }.responseString { req, res, result ->
            request = req
            response = res
            val (d, err) = result
            data = d
            error = err
        }

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, notNullValue())
        assertThat(data, nullValue())

        assertThat(error?.exception as FileNotFoundException, isA(FileNotFoundException::class.java))

        val statusCode = -1
        assertThat(response?.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpUploadWithMultipleFiles() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.upload("/post", param = listOf("foo" to "bar"))
                .sources { _, _ ->
                    listOf(File(currentDir, "lorem_ipsum_short.tmp"),
                            File(currentDir, "lorem_ipsum_long.tmp"))
                }
                .name { "file-name" }
                .responseString { req, res, result ->
                    request = req
                    response = res
                    val (d, err) = result
                    data = d
                    error = err
                    print(d)
                }

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val string = data as String
        assertThat(string, containsString("file-name1"))
        assertThat(string, containsString("file-name2"))

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpUploadWithMultipleDataParts() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.upload("/post", param = listOf("foo" to "bar"))
                .dataParts { _, _ ->
                    listOf(
                            DataPart(File(currentDir, "lorem_ipsum_short.tmp"), type = "image/jpeg"),
                            DataPart(File(currentDir, "lorem_ipsum_long.tmp"), name = "second-file", type = "image/jpeg")
                    )
                }
                .responseString { req, res, result ->
                    request = req
                    response = res
                    val (d, err) = result
                    data = d
                    error = err
                    print(d)
                }

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val string = data as String
        assertThat(string, containsString("lorem_ipsum_short"))
        assertThat(string, containsString("second-file"))

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpUploadWithNamedBlob() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null


        val file = File(currentDir, "lorem_ipsum_short.tmp")

        manager.upload("/post", param = listOf("foo" to "bar"))

                .blob { r, _ ->
                    r.name = "coolblob"
                    Blob(inputStream = { file.inputStream() }, length = file.length(), name = file.name)
                }
                .responseString { req, res, result ->
                    request = req
                    response = res
                    val (d, err) = result
                    data = d
                    error = err
                    print(d)
                }

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val string = data as String
        assertThat(string, containsString("coolblob"))

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))
    }
}
