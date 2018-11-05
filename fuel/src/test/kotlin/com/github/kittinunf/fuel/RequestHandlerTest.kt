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
        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/http-get"),
            response = mock.reflect()
        )

        mock.path("http-get").httpGet().response(object : Handler<ByteArray> {
            override fun success(request: Request, response: Response, value: ByteArray) {
                assertThat(request, notNullValue())
                assertThat(response, notNullValue())
                assertThat(value, notNullValue())

                val statusCode = HttpURLConnection.HTTP_OK
                assertThat(response.statusCode, isEqualTo(statusCode))
            }

            override fun failure(request: Request, response: Response, error: FuelError) {
                fail("Expected to not hit failure path")
            }
        })
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
        val data: Any? = null

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/not-found"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        mock.path("not-found").httpGet().response(object : Handler<ByteArray> {

            override fun success(request: Request, response: Response, value: ByteArray) {
                fail("Expected not to hit success path")
            }

            override fun failure(request: Request, response: Response, error: FuelError) {
                assertThat(request, notNullValue())
                assertThat(response, notNullValue())
                assertThat(error, notNullValue())

                val statusCode = HttpURLConnection.HTTP_NOT_FOUND
                assertThat(response.statusCode, isEqualTo(statusCode))
            }
        })

        assertThat(data, nullValue())
    }

    @Test
    fun httpPostRequestWithParameters() {
        val err: FuelError? = null

        val paramKey = "foo"
        val paramValue = "bar"

        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/http-post"),
            response = mock.reflect()
        )

        mock.path("http-post").httpPost(listOf(paramKey to paramValue)).responseString(object : Handler<String> {
            override fun success(request: Request, response: Response, value: String) {
                assertThat(request, notNullValue())
                assertThat(response, notNullValue())
                assertThat(value, notNullValue())

                val statusCode = HttpURLConnection.HTTP_OK
                assertThat(response.statusCode, isEqualTo(statusCode))

                assertThat(value, containsString(paramKey))
                assertThat(value, containsString(paramValue))
            }

            override fun failure(request: Request, response: Response, error: FuelError) {
                fail("Expected not to hit failure path")
            }
        })

        assertThat(err, nullValue())
    }
}
