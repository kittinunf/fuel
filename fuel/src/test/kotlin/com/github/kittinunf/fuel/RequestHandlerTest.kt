package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Handler
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.fail
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.net.HttpURLConnection
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RequestHandlerTest : MockHttpTestCase() {

    init {
        FuelManager.instance.baseHeaders = mapOf("foo" to "bar")
        FuelManager.instance.baseParams = listOf("key" to "value")
    }

    @Test
    fun httpGetRequestValid() {
        var req: Request? = null
        var res: Response? = null
        var data: Any? = null
        val err: FuelError? = null

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/http-get"),
            response = mock.reflect()
        )

        mock.path("http-get").httpGet().response(object : Handler<ByteArray> {
            override fun success(request: Request, response: Response, value: ByteArray) {
                req = request
                res = response
                data = value
            }

            override fun failure(request: Request, response: Response, error: FuelError) {
                fail("Expected to not hit failure path")
            }
        })

        assertThat(req, notNullValue())
        assertThat(res, notNullValue())
        assertThat(err, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(res?.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpGetRequestWithMalformedHeaders() {
        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/http-get"),
            response = mock.reflect()
        )

        mock.path("http-get").httpGet().header("sample" to "a\nb\nc").response().third.fold({ _ ->
            fail()
        }, { e ->
            e.printStackTrace()
            assertTrue(e.exception is IllegalArgumentException)
        })
    }

    @Test
    fun httpGetRequestInvalid() {
        var req: Request? = null
        var res: Response? = null
        val data: Any? = null
        var err: FuelError? = null

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/not-found"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        mock.path("not-found").httpGet().response(object : Handler<ByteArray> {

            override fun success(request: Request, response: Response, value: ByteArray) {
                fail("Expected not to hit success path")
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
        assertThat(res?.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpPostRequestWithParameters() {
        var req: Request? = null
        var res: Response? = null
        var data: Any? = null
        val err: FuelError? = null

        val paramKey = "foo"
        val paramValue = "bar"

        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/http-post"),
            response = mock.reflect()
        )

        mock.path("http-post").httpPost(listOf(paramKey to paramValue)).responseString(object : Handler<String> {
            override fun success(request: Request, response: Response, value: String) {
                req = request
                res = response
                data = value
            }

            override fun failure(request: Request, response: Response, error: FuelError) {
                fail("Expected not to hit failure path")
            }
        })

        val string = data as String

        assertThat(req, notNullValue())
        assertThat(res, notNullValue())
        assertThat(err, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(res?.statusCode, isEqualTo(statusCode))

        assertThat(string, containsString(paramKey))
        assertThat(string, containsString(paramValue))
    }
}
