package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.interceptors.cUrlLoggingRequestInterceptor
import com.github.kittinunf.fuel.core.interceptors.loggingRequestInterceptor
import com.github.kittinunf.fuel.core.interceptors.loggingResponseInterceptor
import com.github.kittinunf.fuel.core.interceptors.validatorResponseInterceptor
import com.github.kittinunf.fuel.util.encodeBase64ToString
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.net.HttpURLConnection
import java.util.*
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
        manager.addRequestInterceptor(loggingRequestInterceptor())

        val (request, response, result) = manager.request(Method.GET, mock.path("get")).response()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat("Expected request to be logged", outContent.toString(), containsString(request.toString()))
        assertThat("Expected response not to be logged", outContent.toString(), not(containsString(response.toString())))

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
        manager.removeRequestInterceptor(loggingRequestInterceptor())
    }

    @Test
    fun testWithLoggingResponseInterceptor() {
        val httpRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mock.chain(request = httpRequest, response = mock.reflect())

        val manager = FuelManager()
        manager.addResponseInterceptor { loggingResponseInterceptor() }

        val (request, response, result) = manager.request(Method.GET, mock.path("get")).response()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        // TODO: remove request from response logger
        //   currently the response logger actually logs both request and response, after the
        //   response comes back. Preferably the requestLogger logs the request as it goes out and
        //   the responseLogger logs the response as it comes in.

        assertThat("Expected request to be logged", outContent.toString(), containsString(request.toString()))
        assertThat("Expected response to be logged", outContent.toString(), containsString(response.toString()))

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
        manager.removeResponseInterceptor { loggingResponseInterceptor() }
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
                println("1: ${t.toString()}")
                interceptorCalled = true
                next(t)
            }
        }

        manager.apply {
            addRequestInterceptor(cUrlLoggingRequestInterceptor())
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
        fun <T> customLoggingBreakingInterceptor() = { _: (T) -> T ->
            { t: T ->
                println("1: ${t.toString()}")
                interceptorCalled = true
                //if next is not called, next Interceptor will not be called as well
                t
            }
        }

        var interceptorNotCalled = true
        fun <T> customLoggingInterceptor() = { next: (T) -> T ->
            { t: T ->
                println("1: ${t.toString()}")
                interceptorNotCalled = false
                next(t)
            }
        }

        manager.apply {
            addRequestInterceptor(cUrlLoggingRequestInterceptor())
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
    fun testWithRedirectInterceptor() {
        val firstRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader("Location", mock.path("redirected"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val redirectedRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirected")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = redirectedRequest, response = mock.reflect())

        val manager = FuelManager()
        val (request, response, result) = manager.request(Method.GET, mock.path("redirect")).header(mapOf("User-Agent" to "Fuel")).response()

        val (data, error) = result
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
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
        manager.addRequestInterceptor(cUrlLoggingRequestInterceptor())
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
    fun testWithRedirectInterceptorRelative() {
        val firstRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader("Location", mock.path("redirected"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val redirectedRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirected")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = redirectedRequest, response = mock.reflect())

        val manager = FuelManager()
        manager.addRequestInterceptor(cUrlLoggingRequestInterceptor())

        val (request, response, result) = manager.request(Method.GET, mock.path("redirect")).header(mapOf("User-Agent" to "Fuel")).response()

        val (data, error) = result
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
    }

    @Test
    fun testWithRedirectInterceptorPreservesBaseHeaders() {
        val firstRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader("Location", "/redirected")
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val redirectedRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirected")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = redirectedRequest, response = mock.reflect())

        val manager = FuelManager()
        manager.addRequestInterceptor(cUrlLoggingRequestInterceptor())

        manager.baseHeaders = mapOf("User-Agent" to "Fuel")

        val (request, response, result) = manager.request(Method.GET, mock.path("redirect")).header(mapOf("User-Agent" to "Fuel")).responseString(Charsets.UTF_8)

        val (data, error) = result
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, containsString("\"User-Agent\":[\"Fuel\"]"))

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
    }

    @Test
    fun testNestedRedirectWithRedirectInterceptor() {
        val firstRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader("Location", mock.path("intermediary"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val secondRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/intermediary")

        val secondResponse = mock.response()
                .withHeader("Location", mock.path("redirected"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val redirectedRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirected")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = secondRequest, response = secondResponse)
        mock.chain(request = redirectedRequest, response = mock.reflect())

        val manager = FuelManager()
        manager.addRequestInterceptor(cUrlLoggingRequestInterceptor())

        val (request, response, result) = manager.request(Method.GET, mock.path("redirect")).header(mapOf("User-Agent" to "Fuel")).responseString(Charsets.UTF_8)

        val (data, error) = result
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
    }

    @Test
    fun testHttpExceptionWithValidatorInterceptor() {
        val firstRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/invalid")

        val firstResponse = mock.response()
                .withStatusCode(418) // I'm a teapot

        mock.chain(request = firstRequest, response = firstResponse)

        val manager = FuelManager()
        manager.addResponseInterceptor(validatorResponseInterceptor(200..299))
        manager.addRequestInterceptor(cUrlLoggingRequestInterceptor())

        val (request, response, result) = manager.request(Method.GET, mock.path("invalid")).responseString(Charsets.UTF_8)

        val (data, error) = result
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, notNullValue())
        assertThat(data, nullValue())

        assertThat(response.statusCode, isEqualTo(418))
    }

    @Test
    fun testHttpExceptionWithRemoveInterceptors() {
        val firstRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/invalid")

        val firstResponse = mock.response()
                .withStatusCode(418) // I'm a teapot

        mock.chain(request = firstRequest, response = firstResponse)

        val manager = FuelManager()
        manager.removeAllResponseInterceptors()

        val (request, response, result) = manager.request(Method.GET, mock.path("invalid")).responseString(Charsets.UTF_8)

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
    fun failsIfRedirectedToResourceReturning404() {
        val firstRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader("Location", mock.path("not-found"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val secondRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/not-found")

        val secondResponse = mock.response()
                .withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = secondRequest, response = secondResponse)

        val manager = FuelManager()
        val (_, _, result) = manager.request(Method.GET, mock.path("redirect")).response()

        val (data, error) = result

        assertThat(error, notNullValue())
        assertThat(data, nullValue())
    }

    @Test
    fun testGet301Redirect() {
        val testValidator = "state:${Random().nextDouble()}"

        val firstRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader("Location", mock.path("get?validate=$testValidator"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_PERM)

        val secondRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = secondRequest, response = mock.reflect())

        val manager = FuelManager()
        val (_, _, result) = manager.request(Method.GET, mock.path("redirect")).responseString()
        val (data, error) = result

        assertThat(data, notNullValue())
        assertThat(data, containsString(testValidator))
        assertThat(error, nullValue())
    }

    @Test
    fun testGet302Redirect() {
        val testValidator = "state:${Random().nextDouble()}"

        val firstRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader("Location", mock.path("get?validate=$testValidator"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val secondRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = secondRequest, response = mock.reflect())

        val manager = FuelManager()
        val (_, _, result) = manager.request(Method.GET, mock.path("redirect")).responseString()
        val (data, error) = result

        assertThat(data, notNullValue())
        assertThat(data, containsString(testValidator))
        assertThat(error, nullValue())
    }

    @Test
    fun testGet303Redirect() {
        val testValidator = "state:${Random().nextDouble()}"

        val firstRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader("Location", mock.path("get?validate=$testValidator"))
                .withStatusCode(HttpURLConnection.HTTP_SEE_OTHER)

        val secondRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = secondRequest, response = mock.reflect())

        val manager = FuelManager()
        val (_, _, result) = manager.request(Method.GET, mock.path("redirect")).responseString()
        val (data, error) = result

        assertThat(data, notNullValue())
        assertThat(data, containsString(testValidator))
        assertThat(error, nullValue())
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
        val (_, _, result) = manager.request(Method.GET, mock.path("not-modified")).responseString()
        val (data, error) = result

        // TODO: Not Modified should not be an error
        assertThat(data, nullValue())
        assertThat(error, notNullValue())
    }

    @Test
    fun testGetRedirectNoUrl() {
        val firstRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val secondRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = secondRequest, response = mock.reflect())

        val manager = FuelManager()
        val (_, _, result) = manager.request(Method.GET, mock.path("redirect")).responseString()
        val (data, error) = result

        assertThat(data, nullValue())
        assertThat(error, notNullValue())
    }

    @Test
    fun testGetWrongUrl() {
        val firstRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader("Location", mock.path("not-found"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val secondRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/not-found")

        val secondResponse = mock.response()
                .withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = secondRequest, response = secondResponse)

        val manager = FuelManager()
        val (_, _, result) = manager.request(Method.GET, mock.path("redirect")).responseString()
        val (data, error) = result

        assertThat(data, nullValue())
        assertThat(error, notNullValue())
    }

    @Test
    fun testPost301Redirect() {
        val firstRequest = mock.request()
                .withMethod(Method.POST.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader("Location", mock.path("get"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_PERM)

        val secondRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = secondRequest, response = mock.reflect())

        val manager = FuelManager()
        val requests = mutableListOf<Request>()

        manager.addRequestInterceptor { next: (Request) -> Request ->
            { r: Request ->
                requests.add(r)
                next(r)
            }
        }

        val (originalRequest, _, result) = manager.request(Method.POST, mock.path("redirect")).responseString()
        val (data, error) = result

        assertThat(data, notNullValue())
        assertThat(error, nullValue())
        assertThat(originalRequest.method, isEqualTo(Method.POST))
        assertThat(requests[1].method, isEqualTo(Method.GET))
    }

    @Test
    fun testPost302Redirect() {
        val firstRequest = mock.request()
                .withMethod(Method.POST.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader("Location", mock.path("get"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val secondRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = secondRequest, response = mock.reflect())

        val manager = FuelManager()
        val requests = mutableListOf<Request>()

        manager.addRequestInterceptor { next: (Request) -> Request ->
            { r: Request ->
                requests.add(r)
                next(r)
            }
        }

        val (originalRequest, _, result) = manager.request(Method.POST, mock.path("redirect")).responseString()
        val (data, error) = result

        assertThat(data, notNullValue())
        assertThat(error, nullValue())
        assertThat(originalRequest.method, isEqualTo(Method.POST))
        assertThat(requests[1].method, isEqualTo(Method.GET))
    }

    @Test
    fun testPost303Redirect() {
        val firstRequest = mock.request()
                .withMethod(Method.POST.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader("Location", mock.path("get"))
                .withStatusCode(HttpURLConnection.HTTP_SEE_OTHER)

        val secondRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = secondRequest, response = mock.reflect())

        val manager = FuelManager()
        val requests = mutableListOf<Request>()

        manager.addRequestInterceptor { next: (Request) -> Request ->
            { r: Request ->
                requests.add(r)
                next(r)
            }
        }

        val (originalRequest, _, result) = manager.request(Method.POST, mock.path("redirect")).responseString()
        val (data, error) = result

        assertThat(data, notNullValue())
        assertThat(error, nullValue())
        assertThat(originalRequest.method, isEqualTo(Method.POST))
        assertThat(requests[1].method, isEqualTo(Method.GET))
    }

    @Test
    fun testPost307Redirect() {
        val firstRequest = mock.request()
                .withMethod(Method.POST.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader("Location", mock.path("post"))
                .withStatusCode(307)

        val secondRequest = mock.request()
                .withMethod(Method.POST.value)
                .withPath("/post")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = secondRequest, response = mock.reflect())

        val manager = FuelManager()
        val requests = mutableListOf<Request>()

        manager.addRequestInterceptor { next: (Request) -> Request ->
            { r: Request ->
                requests.add(r)
                next(r)
            }
        }

        val (originalRequest, _, result) = manager.request(Method.POST, mock.path("redirect")).responseString()
        val (data, error) = result

        assertThat(data, notNullValue())
        assertThat(error, nullValue())
        assertThat(originalRequest.method, isEqualTo(Method.POST))
        assertThat(requests[1].method, isEqualTo(Method.POST))
    }

    @Test
    fun testPost308Redirect() {
        val firstRequest = mock.request()
                .withMethod(Method.POST.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader("Location", mock.path("post"))
                .withStatusCode(308)

        val secondRequest = mock.request()
                .withMethod(Method.POST.value)
                .withPath("/post")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = secondRequest, response = mock.reflect())

        val manager = FuelManager()
        val requests = mutableListOf<Request>()

        manager.addRequestInterceptor { next: (Request) -> Request ->
            { r: Request ->
                requests.add(r)
                next(r)
            }
        }

        val (originalRequest, _, result) = manager.request(Method.POST, mock.path("redirect")).responseString()
        val (data, error) = result

        assertThat(data, notNullValue())
        assertThat(error, nullValue())
        assertThat(originalRequest.method, isEqualTo(Method.POST))
        assertThat(requests[1].method, isEqualTo(Method.POST))
    }

    @Test
    fun testHeaderIsPassingAlongWithRedirection() {
        val testValidator = "state:${Random().nextDouble()}"

        val firstRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader("Location", mock.path("get?validate=$testValidator"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val secondRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = secondRequest, response = mock.reflect())

        val manager = FuelManager()
        val (_, _, result) = manager.request(Method.GET, mock.path("redirect"))
                .header("Foo" to "bar")
                .responseString()
        val (data, error) = result

        assertThat(data, notNullValue())
        assertThat(data, containsString(testValidator))
        assertThat(data, containsString("\"Foo\":[\"bar\"]"))
        assertThat(error, nullValue())
    }

    @Test
    fun testHeaderIsPassingAlongWithRedirectionWithinSubPath() {
        val firstRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader("Location", mock.path("basic-auth/user/pass"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val username = "user"
        val password = "pass"
        val auth = "$username:$password"
        val encodedAuth = auth.encodeBase64ToString()

        val secondRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/basic-auth/user/pass")
                .withHeader("Authorization", "Basic $encodedAuth")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = secondRequest, response = mock.reflect())

        val manager = FuelManager()

        val (_, _, result) = manager.request(Method.GET, mock.path("redirect"))
                .header("Foo" to "bar")
                .authenticate(username, password)
                .responseString()

        val (data, error) = result
        assertThat(data, notNullValue())
        assertThat(error, nullValue())
    }

    @Test
    fun testHeaderAuthenticationWillBeRemoveIfRedirectToDifferentHost() {
        val firstRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader("Location", mock.path("basic-auth/user/pass").replace("localhost", "127.0.0.1"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val username = "user"
        val password = "pass"

        val secondRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/basic-auth/user/pass")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = secondRequest, response = mock.reflect())

        val manager = FuelManager()

        val requests = mutableListOf<Request>()

        manager.addRequestInterceptor { next: (Request) -> Request ->
            { r: Request ->
                requests.add(r)
                next(r)
            }
        }

        val (_, _, result) = manager.request(Method.GET, mock.path("redirect"))
                .header("Foo" to "bar")
                .authenticate(username, password)
                .responseString()

        val (data, error) = result

        assertThat(data, notNullValue())
        assertThat(error, nullValue())
        assertThat(requests[1].headers["Foo"], notNullValue())
        assertThat(requests[1].headers["Authorization"], nullValue())
    }

    @Test
    fun testDoNotAllowRedirect() {
        val firstRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader("Location", mock.path("get"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        mock.chain(request = firstRequest, response = firstResponse)

        val manager = FuelManager()
        val (_, _, result) = manager.request(Method.GET, mock.path("redirect"))
                .allowRedirects(false)
                .responseString()

        val (data, error) = result

        // TODO: This is current based on the current behavior, however we need to fix this as it should handle 100 - 399 gracefully not httpException
        assertThat(data, nullValue())
        assertThat(error, notNullValue())
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
