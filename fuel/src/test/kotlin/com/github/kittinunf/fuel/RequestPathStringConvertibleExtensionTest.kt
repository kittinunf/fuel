package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.*
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.io.File
import java.net.HttpURLConnection
import java.util.concurrent.CountDownLatch
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RequestPathStringConvertibleExtensionTest : BaseTestCase() {

    init {
        Manager.instance.basePath = "https://httpbin.org"
    }

    enum class HttpsBin(val relativePath: String) : Fuel.PathStringConvertible {
        COOKIES("cookies"),
        POST("post"),
        PUT("put"),
        DELETE("delete"),
        DOWNLOAD("bytes/123456"),
        UPLOAD("post");

        override val path = "/$relativePath"
    }

    @Before
    fun setUp() {
        lock = CountDownLatch(1)
    }

    @Test
    fun httpGetRequestWithSharedInstance() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        HttpsBin.COOKIES.httpGet().responseString { req, res, result ->
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
    fun httpPostRequestWithSharedInstance() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        HttpsBin.POST.httpPost().responseString { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            data = d
            error = err

            lock.countDown()
        }

        await()

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.httpStatusCode, isEqualTo(statusCode))

        assertThat(string, containsString("https"))
    }

    @Test
    fun httpPutRequestWithSharedInstance() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        HttpsBin.PUT.httpPut().responseString { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            data = d
            error = err

            lock.countDown()
        }

        await()

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.httpStatusCode, isEqualTo(statusCode))

        assertThat(string, containsString("https"))
    }

    @Test
    fun httpDeleteRequestWithSharedInstance() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        HttpsBin.DELETE.httpDelete().responseString { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            data = d
            error = err

            lock.countDown()
        }

        await()

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.httpStatusCode, isEqualTo(statusCode))

        assertThat(string, containsString("https"))
    }

    @Test
    fun httpUploadRequestWithSharedInstance() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        HttpsBin.UPLOAD.httpUpload().source { request, url ->
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
    fun httpDownloadRequestWithSharedInstance() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        HttpsBin.DOWNLOAD.httpDownload().destination { response, url ->
            File.createTempFile(123456.toString(), null)
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