package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RequestDownloadTest : BaseTestCase() {
    private val manager: FuelManager by lazy {
        FuelManager().apply {
            basePath = "http://httpbin.org"
        }
    }

    @Test
    fun httpDownloadCase() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        val numberOfBytes = 32768L
        val file = File.createTempFile(numberOfBytes.toString(), null)

        manager.download("/bytes/$numberOfBytes").destination { _, _ ->
            println(file.absolutePath)
            file
        }.response{ req, res, result ->
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
        assertThat(file.length(),isEqualTo(numberOfBytes))

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpDownloadWithProgressValidCaseResponse() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        var read = -1L
        var total = -1L

        val numberOfBytes = 1186L
        val file = File.createTempFile(numberOfBytes.toString(), null)

        manager.download("/bytes/$numberOfBytes").destination { _, _ ->
            println(file.absolutePath)
            file
        }.progress { readBytes, totalBytes ->
            read = readBytes
            total = totalBytes
            println("read: $read, total: $total")
        }.response { req, res, result ->
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
        assertEquals(data is ByteArray,true)
        assertEquals((data as ByteArray).size.toLong(),read)
        assertThat(file.length(),isEqualTo(numberOfBytes))

        assertThat("read bytes and total bytes should be equal", read == total && read != -1L && total != -1L, isEqualTo(true))
        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpDownloadWithProgressValidCase() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        var read = -1L
        var total = -1L

        val numberOfBytes = 45024L
        val file = File.createTempFile(numberOfBytes.toString(), null)

        manager.download("/bytes/$numberOfBytes").destination { _, _ ->
            println(file.absolutePath)
            file
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

        assertThat(file.length(),isEqualTo(response?.data?.size?.toLong() ?: 0L))
        assertThat(file.length(),isEqualTo(numberOfBytes))

        assertThat("read bytes and total bytes should be equal", read == total && read != -1L && total != -1L, isEqualTo(true))
        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpDownloadWithProgressInvalidEndPointCase() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        val numberOfBytes = 131072L
        val file = File.createTempFile(numberOfBytes.toString(), null)

        manager.download("/byte/$numberOfBytes").destination { _, _ ->
            println(file.absolutePath)
            file
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
        assertThat(file.length(),isEqualTo(0L))

        val statusCode = HttpURLConnection.HTTP_NOT_FOUND
        assertThat(response?.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpDownloadWithProgressInvalidFileCase() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        val numberOfBytes = 131072L
        manager.download("/bytes/$numberOfBytes").destination { _, _ ->
            val dir = System.getProperty("user.dir")
            File.createTempFile("not_found_file", null, File(dir, "not-a-folder"))
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

        val statusCode = -1
        assertThat(error?.exception as IOException, isA(IOException::class.java))
        assertThat(response?.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpDownloadBigFileWithProgressValidCase() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        var read = -1L
        var total = -1L
        var lastPercent = 0L

        val file = File.createTempFile("100MB.zip", null)
        manager.download("http://speedtest.tele2.net/100MB.zip").destination { _, _ ->
            println(file.absolutePath)
            file
        }.progress { readBytes, totalBytes ->
            read = readBytes
            total = totalBytes
            val percent = readBytes * 100 / totalBytes
            if (percent > lastPercent) {
                println("read: $read, total: $total, $percent% ")
                lastPercent = percent
            }
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
        assertThat(file.length(),isEqualTo(100L * 1024L * 1024L))

        assertThat("read bytes and total bytes should be equal", read == total && read != -1L && total != -1L, isEqualTo(true))
        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))
    }

}
