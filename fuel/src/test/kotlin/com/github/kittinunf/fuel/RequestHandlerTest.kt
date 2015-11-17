package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.*
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection
import java.util.concurrent.CountDownLatch
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Created by Kittinun Vantasin on 8/18/15.
 */

class RequestHandlerTest : BaseTestCase() {

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

                lock.countDown()
            }

            override fun failure(request: Request, response: Response, error: FuelError) {
                println(error)
            }

        })

        await()

        assertNotNull(req, "request should not be null")
        assertNotNull(res, "response should not be null")
        assertNull(err, "error should be null")
        assertNotNull(data, "data should not be null")
        assertTrue(res?.httpStatusCode == HttpURLConnection.HTTP_OK, "http status code should be ${HttpURLConnection.HTTP_OK}")
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

                lock.countDown()
            }

        })

        await()

        assertNotNull(req, "request should not be null")
        assertNotNull(res, "response should not be null")
        assertNotNull(err, "error should not be null")
        assertNull(data, "data should be null")
        assertTrue(res?.httpStatusCode == HttpURLConnection.HTTP_NOT_FOUND, "http status code (${res?.httpStatusCode}) should be ${HttpURLConnection.HTTP_NOT_FOUND}")
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

                lock.countDown()
            }

            override fun failure(request: Request, response: Response, error: FuelError) {
                println(error)
            }
        })

        await()

        val string = data as String

        assertNotNull(req, "request should not be null")
        assertNotNull(res, "response should not be null")
        assertNull(err, "error should be null")
        assertNotNull(data, "data should not be null")
        assertTrue(res?.httpStatusCode == HttpURLConnection.HTTP_OK, "http status code should be ${HttpURLConnection.HTTP_OK}")

        assertTrue(string.contains(paramKey) && string.contains(paramValue), "url query param should be sent along with url and present in response of httpbin.org")
    }



}