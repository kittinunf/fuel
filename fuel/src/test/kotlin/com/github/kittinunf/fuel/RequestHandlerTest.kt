package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.*
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Test
import java.net.HttpURLConnection
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RequestHandlerTest : BaseTestCase() {

    init {
        Manager.instance.basePath = "https://httpbin.org"
        Manager.instance.baseHeaders = mapOf("foo" to "bar")
        Manager.instance.baseParams = listOf("key" to "value")
    }

    @Test
    fun httpGetRequestValid() {
        var req: Request? = null
        var res: Response? = null
        var data: Any? = null
        var err: FuelError? = null

        "/get".httpGet().response(object : Handler<ByteArray> {

            override fun success(request: Request, response: Response, value: ByteArray) {
                req = request
                res = response
                data = value
            }

            override fun failure(request: Request, response: Response, error: FuelError) {
                println(error)
            }

        })

        assertThat(req, notNullValue())
        assertThat(res, notNullValue())
        assertThat(err, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(res?.httpStatusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpGetRequestInvalid() {
        var req: Request? = null
        var res: Response? = null
        var data: Any? = null
        var err: FuelError? = null

        "/g".httpGet().response(object : Handler<ByteArray> {

            override fun success(request: Request, response: Response, value: ByteArray) {
                println(String(value))
            }

            override fun failure(request: Request, response: Response, error: FuelError) {
                req = request
                res = response
                err = error
            }

        })

        assertThat(req, notNullValue())
        assertThat(res, notNullValue())
        assertThat(err, notNullValue())
        assertThat(data, nullValue())

        val statusCode = HttpURLConnection.HTTP_NOT_FOUND
        assertThat(res?.httpStatusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpPostRequestWithParameters() {
        var req: Request? = null
        var res: Response? = null
        var data: Any? = null
        var err: FuelError? = null

        val paramKey = "foo"
        val paramValue = "bar"

        "/post".httpPost(listOf(paramKey to paramValue)).responseString(object : Handler<String> {
            override fun success(request: Request, response: Response, value: String) {
                req = request
                res = response
                data = value
            }

            override fun failure(request: Request, response: Response, error: FuelError) {
                println(error)
            }
        })

        val string = data as String

        assertThat(req, notNullValue())
        assertThat(res, notNullValue())
        assertThat(err, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(res?.httpStatusCode, isEqualTo(statusCode))

        assertThat(string, containsString(paramKey))
        assertThat(string, containsString(paramValue))
    }

}