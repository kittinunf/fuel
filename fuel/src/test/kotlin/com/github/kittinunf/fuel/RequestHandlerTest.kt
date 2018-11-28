package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.ResponseHandler
import com.github.kittinunf.fuel.test.MockHttpTestCase
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.fail
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit

class RequestHandlerTest : MockHttpTestCase() {

    @Before
    fun setupFuelManager() {
        FuelManager.instance.baseHeaders = mapOf("foo" to "bar")
        FuelManager.instance.baseParams = listOf("key" to "value")
    }

    @After
    fun resetFuelManager() {
        FuelManager.instance.reset()
    }

    @Test
    fun httpGetRequestValid() {
        var isAsync = false
        var isHandled = false

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/http-get"),
            response = mock.reflect().withDelay(TimeUnit.MILLISECONDS, 1_000)
        )

        val running = mock.path("http-get").httpGet().response(object : ResponseHandler<ByteArray> {
            override fun success(request: Request, response: Response, value: ByteArray) {
                assertThat(request, notNullValue())
                assertThat(response, notNullValue())
                assertThat(value, notNullValue())

                val statusCode = HttpURLConnection.HTTP_OK
                assertThat(response.statusCode, equalTo(statusCode))
                assertThat(isAsync, equalTo(true))

                isHandled = true
            }

            override fun failure(request: Request, response: Response, error: FuelError) {
                fail("Expected to not hit failure path")
            }
        })

        isAsync = true
        running.join()

        assertThat(running.isDone, equalTo(true))
        assertThat(isHandled, equalTo(true))
    }

    @Test
    fun httpGetRequestWithMalformedHeaders() {
        var isAsync = false
        var isHandled = false

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/http-get"),
            response = mock.reflect().withDelay(TimeUnit.MILLISECONDS, 1_000)
        )

        val running = mock.path("http-get")
            .httpGet()
            .header("sample" to "a\nb\nc")
            .response { _, _, result ->
                result.fold(
                    { fail("Expected IllegalArgumentException, actual $it") },
                    { e ->
                        assertTrue(e.exception is IllegalArgumentException)
                        assertThat(isAsync, equalTo(true))

                        isHandled = true
                    }
                )
            }

        isAsync = true
        running.join()

        assertThat(running.isDone, equalTo(true))
        assertThat(isHandled, equalTo(true))
    }

    @Test
    fun httpGetRequestInvalid() {
        var isAsync = false
        var isHandled = false

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/not-found"),
            response = mock.response()
                .withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
                .withDelay(TimeUnit.MILLISECONDS, 1_000)
        )

        val running = mock.path("not-found")
            .httpGet()
            .response(object : ResponseHandler<ByteArray> {
                override fun success(request: Request, response: Response, value: ByteArray) {
                    fail("Expected not to hit success path")
                }

                override fun failure(request: Request, response: Response, error: FuelError) {
                    assertThat(request, notNullValue())
                    assertThat(response, notNullValue())
                    assertThat(error, notNullValue())

                    val statusCode = HttpURLConnection.HTTP_NOT_FOUND
                    assertThat(response.statusCode, equalTo(statusCode))
                    assertThat(isAsync, equalTo(true))

                    isHandled = true
                }
            })

        isAsync = true
        running.join()

        assertThat(running.isDone, equalTo(true))
        assertThat(isHandled, equalTo(true))
    }

    @Test
    fun httpPostRequestWithParameters() {
        var isAsync = false
        var isHandled = false

        val paramKey = "foo"
        val paramValue = "bar"

        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/http-post"),
            response = mock.reflect().withDelay(TimeUnit.MILLISECONDS, 1_000)
        )

        val running = mock.path("http-post")
            .httpPost(listOf(paramKey to paramValue))
            .responseString(object : ResponseHandler<String> {
                override fun success(request: Request, response: Response, value: String) {
                    assertThat(request, notNullValue())
                    assertThat(response, notNullValue())
                    assertThat(value, notNullValue())

                    val statusCode = HttpURLConnection.HTTP_OK
                    assertThat(response.statusCode, equalTo(statusCode))

                    assertThat(value, containsString(paramKey))
                    assertThat(value, containsString(paramValue))
                    assertThat(isAsync, equalTo(true))

                    isHandled = true
                }

                override fun failure(request: Request, response: Response, error: FuelError) {
                    fail("Expected not to hit failure path")
                }
            })

        isAsync = true
        running.join()

        assertThat(running.isDone, equalTo(true))
        assertThat(isHandled, equalTo(true))
    }
}
