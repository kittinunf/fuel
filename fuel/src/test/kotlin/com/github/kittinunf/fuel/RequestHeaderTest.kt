package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.*
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection
import java.util.concurrent.CountDownLatch
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RequestHeaderTest : BaseTestCase() {

    val manager: Manager by lazy {
        Manager().apply {
            basePath = "http://httpbin.org"
        }
    }

    @Before
    fun setUp() {
        lock = CountDownLatch(1)
    }

    @Test
    fun httpRequestHeader() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        val headerKey = "Custom"
        val headerValue = "foobar"

        manager.request(Method.GET, "/get").header(headerKey to headerValue).response { req, res, result ->
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

        val string = String(data as ByteArray)
        assertThat(string, containsString(headerKey))
        assertThat(string, containsString(headerValue))
    }

}