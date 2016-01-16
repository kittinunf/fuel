package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.*
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.io.File
import java.net.HttpURLConnection
import java.util.concurrent.CountDownLatch
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RequestStringExtensionTest : BaseTestCase() {

    init {
        Manager.instance.basePath = "https://httpbin.org"
        Manager.instance.baseHeaders = mapOf("foo" to "bar")
        Manager.instance.baseParams = listOf("key" to "value")
    }

    @Before
    fun setUp() {
        lock = CountDownLatch(1)
    }

    @Test
    fun httpGet() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        "/get".httpGet().responseString { req, res, result ->
            request = req
            response = res
            val (d, err) = result
            data = d
            error = err

            lock.countDown()
        }

        await()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.httpStatusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpPost() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        "/post".httpPost().responseString { req, res, result ->
            request = req
            response = res
            val (d, err) = result
            data = d
            error = err

            lock.countDown()
        }

        await()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.httpStatusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpPut() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        "/put".httpPut().responseString { req, res, result ->
            request = req
            response = res
            val (d, err) = result
            data = d
            error = err

            lock.countDown()
        }

        await()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.httpStatusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpDelete() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        "/delete".httpDelete().responseString { req, res, result ->
            request = req
            response = res
            val (d, err) = result
            data = d
            error = err

            lock.countDown()
        }

        await()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.httpStatusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpDownload() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        val numberOfBytes = 32768L

        "/bytes/$numberOfBytes".httpDownload().destination { response, url ->
            val f = File.createTempFile(numberOfBytes.toString(), null)
            println(f.absolutePath)
            f
        }.responseString { req, res, result ->
            request = req
            response = res
            val (d, err) = result
            data = d
            error = err

            lock.countDown()
        }

        await()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.httpStatusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpUploadWithPut() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        "/put".httpUpload(Method.PUT).source { request, url ->
            val dir = System.getProperty("user.dir")
            val currentDir = File(dir, "src/test/assets")
            File(currentDir, "lorem_ipsum_long.tmp")
        }.responseString { req, res, result ->
            request = req
            response = res
            val (d, err) = result
            data = d
            error = err

            lock.countDown()
        }

        await()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.httpStatusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpUploadWithPost() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        "/post".httpUpload().source { request, url ->
            val dir = System.getProperty("user.dir")
            val currentDir = File(dir, "src/test/assets")
            File(currentDir, "lorem_ipsum_long.tmp")
        }.responseString { req, res, result ->
            request = req
            response = res
            val (d, err) = result
            data = d
            error = err

            lock.countDown()
        }

        await()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.httpStatusCode, isEqualTo(statusCode))
    }

}