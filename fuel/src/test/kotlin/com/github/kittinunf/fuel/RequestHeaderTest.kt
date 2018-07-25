package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.*
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Test
import java.net.HttpURLConnection
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RequestHeaderTest : BaseTestCase() {
    private val manager: FuelManager by lazy {
        FuelManager().apply {
            basePath = "http://httpbin.org"
        }
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
        }

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))

        val string = String(data as ByteArray)
        assertThat(string, containsString(headerKey))
        assertThat(string, containsString(headerValue))
    }

    @Test
    fun multipleHeadersByTheSameKeyWillBeCorrectlyFormatted(){
        val request = manager.request(Method.GET, "/get")
                .header("foo" to "bar","a" to "b", "cookie" to "val1=x", "cookie" to "val2=y","cookie" to "val3=z", "cookie" to "val4=j")

        assertEquals("[ val1=x; val2=y; val3=z; val4=j ]",request.headers["cookie"] )
        assertEquals("bar",request.headers["foo"] )
        assertEquals("b",request.headers["a"] )
    }

    @Test
    fun multipleHeadersByTheSameKeyWillShowLastUsingMap(){
        val request = manager.request(Method.GET, "/get")
                .header(mapOf("cookie" to "val1=x", "cookie" to "val2=y"),false)

        assertEquals("val2=y",request.headers["cookie"] )
    }

}