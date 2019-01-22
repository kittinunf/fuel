package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.extensions.cUrlString
import com.github.kittinunf.fuel.core.interceptors.LogRequestAsCurlInterceptor
import com.github.kittinunf.fuel.core.interceptors.LogRequestInterceptor
import com.github.kittinunf.fuel.core.interceptors.LogResponseInterceptor
import com.github.kittinunf.fuel.test.MockHttpTestCase
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.net.HttpURLConnection
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class InterceptorTest : MockHttpTestCase() {

    private val outContent = ByteArrayOutputStream()
    private val errContent = ByteArrayOutputStream()
    private val originalOut = System.out
    private val originalErr = System.err

    @Before
    fun prepareStream() {
        System.setOut(PrintStream(outContent))
        System.setErr(PrintStream(errContent))
    }

    @After
    fun teardownStreams() {
        System.setOut(originalOut)
        System.setErr(originalErr)
    }

    @Test
    fun testWithNoInterceptor() {
        val httpRequest = mock.request()
            .withMethod(Method.GET.value)
            .withPath("/get")

        mock.chain(request = httpRequest, response = mock.reflect())

        val manager = FuelManager()
        val (request, response, result) = manager.request(Method.GET, mock.path("get")).response()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat("Expected request not to be logged", outContent.toString(), not(containsString(request.toString())))
        assertThat("Expected response not to be logged", outContent.toString(), not(containsString(response.toString())))

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
    }

    @Test
    fun testWithLoggingRequestInterceptor() {
        val httpRequest = mock.request()
            .withMethod(Method.GET.value)
            .withPath("/get")

        mock.chain(request = httpRequest, response = mock.reflect())

        val manager = FuelManager()
        manager.addRequestInterceptor(LogRequestInterceptor)

        val (request, response, result) = manager.request(Method.GET, mock.path("get")).response()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat("Expected request to be logged", outContent.toString(), containsString(request.toString()))
        assertThat("Expected response not to be logged", outContent.toString(), not(containsString(response.toString())))

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
        manager.removeRequestInterceptor(LogRequestInterceptor)
    }

    @Test
    fun testWithLoggingResponseInterceptor() {
        val httpRequest = mock.request()
            .withMethod(Method.GET.value)
            .withPath("/get")

        mock.chain(request = httpRequest, response = mock.reflect())

        val manager = FuelManager()
        manager.addResponseInterceptor(LogResponseInterceptor)

        val (request, response, result) = manager.request(Method.GET, mock.path("get")).response()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat("Expected response to be logged", outContent.toString(), containsString(response.toString()))

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
        manager.removeResponseInterceptor(LogResponseInterceptor)
    }

    @Test
    fun testWithResponseToString() {
        val httpRequest = mock.request()
            .withMethod(Method.GET.value)
            .withPath("/get")

        mock.chain(request = httpRequest, response = mock.reflect())

        val manager = FuelManager()
        val (request, response, result) = manager.request(Method.GET, mock.path("get")).response()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))

        assertThat(response.toString(), containsString("Response :"))
        assertThat(response.toString(), containsString("Length :"))
        assertThat(response.toString(), containsString("Body :"))
        assertThat(response.toString(), containsString("Headers :"))
    }

    @Test
    fun testWithMultipleInterceptors() {
        val httpRequest = mock.request()
            .withMethod(Method.GET.value)
            .withPath("/get")

        mock.chain(request = httpRequest, response = mock.reflect())

        val manager = FuelManager()

        var interceptorCalled = false

        fun <T> customLoggingInterceptor() = { next: (T) -> T ->
            { t: T ->
                println("1: $t")
                interceptorCalled = true
                next(t)
            }
        }

        manager.apply {
            addRequestInterceptor(LogRequestAsCurlInterceptor)
            addRequestInterceptor(customLoggingInterceptor())
        }

        val (request, response, result) = manager.request(Method.GET, mock.path("get")).header(mapOf("User-Agent" to "Fuel")).response()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))

        assertThat("Expected request to be curl logged", outContent.toString(), containsString(request.cUrlString()))
        assertThat(interceptorCalled, isEqualTo(true))
    }

    @Test
    fun testWithBreakingChainInterceptor() {
        val httpRequest = mock.request()
            .withMethod(Method.GET.value)
            .withPath("/get")

        mock.chain(request = httpRequest, response = mock.reflect())

        val manager = FuelManager()

        var interceptorCalled = false

        @Suppress("RedundantLambdaArrow")
        fun <T> customLoggingBreakingInterceptor() = { _: (T) -> T ->
            { t: T ->
                println("1: $t")
                interceptorCalled = true
                // if next is not called, next Interceptor will not be called as well
                t
            }
        }

        var interceptorNotCalled = true
        fun <T> customLoggingInterceptor() = { next: (T) -> T ->
            { t: T ->
                println("1: $t")
                interceptorNotCalled = false
                next(t)
            }
        }

        manager.apply {
            addRequestInterceptor(LogRequestAsCurlInterceptor)
            addRequestInterceptor(customLoggingBreakingInterceptor())
            addRequestInterceptor(customLoggingInterceptor())
        }

        val (request, response, result) = manager.request(Method.GET, mock.path("get")).header(mapOf("User-Agent" to "Fuel")).response()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
        assertThat(interceptorCalled, isEqualTo(true))
        assertThat(interceptorNotCalled, isEqualTo(true))
    }

    @Test
    fun testWithoutDefaultRedirectionInterceptor() {
        val firstRequest = mock.request()
            .withMethod(Method.GET.value)
            .withPath("/redirect")

        val firstResponse = mock.response()
            .withHeader("Location", mock.path("redirected"))
            .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        mock.chain(request = firstRequest, response = firstResponse)

        val manager = FuelManager()
        manager.addRequestInterceptor(LogRequestAsCurlInterceptor)
        manager.removeAllResponseInterceptors()

        val (request, response, result) = manager.request(Method.GET, mock.path("redirect")).header(mapOf("User-Agent" to "Fuel")).response()

        val (data, error) = result
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_MOVED_TEMP))
    }

    @Test
    fun testHttpExceptionWithRemoveValidator() {
        val firstRequest = mock.request()
            .withMethod(Method.GET.value)
            .withPath("/invalid")

        val firstResponse = mock.response()
            .withStatusCode(418) // I'm a teapot

        mock.chain(request = firstRequest, response = firstResponse)

        val manager = FuelManager()

        val (request, response, result) =
                manager.request(Method.GET, mock.path("invalid"))
                        .validate { true }
                        .responseString()

        val (data, error) = result
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.statusCode, isEqualTo(418))
    }

    @Test
    fun failsIfRequestedResourceReturns404() {
        val firstRequest = mock.request()
            .withMethod(Method.GET.value)
            .withPath("/not-found")

        val firstResponse = mock.response()
            .withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)

        mock.chain(request = firstRequest, response = firstResponse)

        val manager = FuelManager()
        val (_, _, result) = manager.request(Method.GET, mock.path("not-found")).response()
        val (data, error) = result

        assertThat(error, notNullValue())
        assertThat(data, nullValue())
    }

    @Test
    fun testGetNotModified() {
        val firstRequest = mock.request()
            .withMethod(Method.GET.value)
            .withPath("/not-modified")

        val firstResponse = mock.response()
            .withStatusCode(HttpURLConnection.HTTP_NOT_MODIFIED)

        mock.chain(request = firstRequest, response = firstResponse)
        val manager = FuelManager()
        val (_, _, result) =
                manager.request(Method.GET, mock.path("not-modified")).responseString()
        val (data, error) = result

        assertThat(data, notNullValue())
        assertThat(error, nullValue())
    }

    @Test
    fun testRemoveAllRequestInterceptors() {
        val firstRequest = mock.request()
            .withMethod(Method.GET.value)
            .withPath("/teapot")

        val firstResponse = mock.response()
            .withStatusCode(418)

        mock.chain(request = firstRequest, response = firstResponse)

        val manager = FuelManager()
        manager.removeAllRequestInterceptors()

        val (request, response, result) = manager.request(Method.GET, mock.path("teapot")).responseString()

        val (data, error) = result
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, notNullValue())
        assertThat(data, nullValue())

        assertThat(response.statusCode, isEqualTo(418))
    }
}
