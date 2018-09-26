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
        val httpRequest = mockRequest()
            .withMethod(Method.GET.value)
            .withPath("/get")

        mockChain(request = httpRequest, response = mockReflect())

        val manager = FuelManager()
        val (request, response, result) = manager.request(Method.GET, mockPath("get")).response()
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
        val httpRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mockChain(request = httpRequest, response = mockReflect())

        val manager = FuelManager()
        manager.addRequestInterceptor(loggingRequestInterceptor())

        val (request, response, result) = manager.request(Method.GET, mockPath("get")).response()
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
        val httpRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mockChain(request = httpRequest, response = mockReflect())

        val manager = FuelManager()
        manager.addResponseInterceptor { loggingResponseInterceptor() }

        val (request, response, result) = manager.request(Method.GET, mockPath("get")).response()
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
        val httpRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mockChain(request = httpRequest, response = mockReflect())

        val manager = FuelManager()
        val (request, response, result) = manager.request(Method.GET, mockPath("get")).response()
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
        val httpRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mockChain(request = httpRequest, response = mockReflect())

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

        val (request, response, result) = manager.request(Method.GET, mockPath("get")).header(mapOf("User-Agent" to "Fuel")).response()
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
        val httpRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mockChain(request = httpRequest, response = mockReflect())

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


        val (request, response, result) = manager.request(Method.GET, mockPath("get")).header(mapOf("User-Agent" to "Fuel")).response()
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
        val firstRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mockResponse()
                .withHeader("Location", mockPath("redirected"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val redirectedRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/redirected")

        mockChain(request = firstRequest, response = firstResponse)
        mockChain(request = redirectedRequest, response = mockReflect())

        val manager = FuelManager()
        val (request, response, result) = manager.request(Method.GET, mockPath("redirect")).header(mapOf("User-Agent" to "Fuel")).response()

        val (data, error) = result
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
    }

    @Test
    fun testWithoutDefaultRedirectionInterceptor() {
        val firstRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mockResponse()
                .withHeader("Location", mockPath("redirected"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        mockChain(request = firstRequest, response = firstResponse)

        val manager = FuelManager()
        manager.addRequestInterceptor(cUrlLoggingRequestInterceptor())
        manager.removeAllResponseInterceptors()

        val (request, response, result) = manager.request(Method.GET, mockPath("redirect")).header(mapOf("User-Agent" to "Fuel")).response()

        val (data, error) = result
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_MOVED_TEMP))
    }

    @Test
    fun testWithRedirectInterceptorRelative() {
        val firstRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mockResponse()
                .withHeader("Location", mockPath("redirected"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val redirectedRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/redirected")

        mockChain(request = firstRequest, response = firstResponse)
        mockChain(request = redirectedRequest, response = mockReflect())

        val manager = FuelManager()
        manager.addRequestInterceptor(cUrlLoggingRequestInterceptor())

        val (request, response, result) = manager.request(Method.GET, mockPath("redirect")).header(mapOf("User-Agent" to "Fuel")).response()

        val (data, error) = result
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
    }

    @Test
    fun testWithRedirectInterceptorPreservesBaseHeaders() {
        val firstRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mockResponse()
                .withHeader("Location", "/redirected")
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val redirectedRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/redirected")

        mockChain(request = firstRequest, response = firstResponse)
        mockChain(request = redirectedRequest, response = mockReflect())

        val manager = FuelManager()
        manager.addRequestInterceptor(cUrlLoggingRequestInterceptor())

        manager.baseHeaders = mapOf("User-Agent" to "Fuel")

        val (request, response, result) = manager.request(Method.GET, mockPath("redirect")).header(mapOf("User-Agent" to "Fuel")).responseString(Charsets.UTF_8)

        val (data, error) = result
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, containsString("\"User-Agent\":[\"Fuel\"]"))

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
    }

    @Test
    fun testNestedRedirectWithRedirectInterceptor() {
        val firstRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mockResponse()
                .withHeader("Location", mockPath("intermediary"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val secondRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/intermediary")

        val secondResponse = mockResponse()
                .withHeader("Location", mockPath("redirected"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val redirectedRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/redirected")

        mockChain(request = firstRequest, response = firstResponse)
        mockChain(request = secondRequest, response = secondResponse)
        mockChain(request = redirectedRequest, response = mockReflect())

        val manager = FuelManager()
        manager.addRequestInterceptor(cUrlLoggingRequestInterceptor())

        val (request, response, result) = manager.request(Method.GET, mockPath("redirect")).header(mapOf("User-Agent" to "Fuel")).responseString(Charsets.UTF_8)

        val (data, error) = result
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
    }

    @Test
    fun testHttpExceptionWithValidatorInterceptor() {
        val firstRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/invalid")

        val firstResponse = mockResponse()
                .withStatusCode(418) // I'm a teapot

        mockChain(request = firstRequest, response = firstResponse)

        val manager = FuelManager()
        manager.addResponseInterceptor(validatorResponseInterceptor(200..299))
        manager.addRequestInterceptor(cUrlLoggingRequestInterceptor())

        val (request, response, result) = manager.request(Method.GET, mockPath("invalid")).responseString(Charsets.UTF_8)

        val (data, error) = result
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, notNullValue())
        assertThat(data, nullValue())

        assertThat(response.statusCode, isEqualTo(418))
    }

    @Test
    fun testHttpExceptionWithRemoveInterceptors() {
        val firstRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/invalid")

        val firstResponse = mockResponse()
                .withStatusCode(418) // I'm a teapot

        mockChain(request = firstRequest, response = firstResponse)

        val manager = FuelManager()
        manager.removeAllResponseInterceptors()

        val (request, response, result) = manager.request(Method.GET, mockPath("invalid")).responseString(Charsets.UTF_8)

        val (data, error) = result
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.statusCode, isEqualTo(418))
    }

    @Test
    fun failsIfRequestedResourceReturns404() {
        val firstRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/not-found")

        val firstResponse = mockResponse()
                .withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)

        mockChain(request = firstRequest, response = firstResponse)

        val manager = FuelManager()
        val (_, _, result) = manager.request(Method.GET, mockPath("not-found")).response()
        val (data, error) = result

        assertThat(error, notNullValue())
        assertThat(data, nullValue())
    }

    @Test
    fun failsIfRedirectedToResourceReturning404() {
        val firstRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mockResponse()
                .withHeader("Location", mockPath("not-found"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val secondRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/not-found")

        val secondResponse = mockResponse()
                .withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)

        mockChain(request = firstRequest, response = firstResponse)
        mockChain(request = secondRequest, response = secondResponse)

        val manager = FuelManager()
        val (_, _, result) = manager.request(Method.GET, mockPath("redirect")).response()

        val (data, error) = result

        assertThat(error, notNullValue())
        assertThat(data, nullValue())
    }

    @Test
    fun testGet301Redirect() {
        val testValidator = "state:${Random().nextDouble()}"

        val firstRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mockResponse()
                .withHeader("Location", mockPath("get?validate=$testValidator"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_PERM)

        val secondRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mockChain(request = firstRequest, response = firstResponse)
        mockChain(request = secondRequest, response = mockReflect())

        val manager = FuelManager()
        val (_, _, result) = manager.request(Method.GET, mockPath("redirect")).responseString()
        val (data, error) = result

        assertThat(data, notNullValue())
        assertThat(data, containsString(testValidator))
        assertThat(error, nullValue())
    }

    @Test
    fun testGet302Redirect() {
        val testValidator = "state:${Random().nextDouble()}"

        val firstRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mockResponse()
                .withHeader("Location", mockPath("get?validate=$testValidator"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val secondRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mockChain(request = firstRequest, response = firstResponse)
        mockChain(request = secondRequest, response = mockReflect())

        val manager = FuelManager()
        val (_, _, result) = manager.request(Method.GET, mockPath("redirect")).responseString()
        val (data, error) = result

        assertThat(data, notNullValue())
        assertThat(data, containsString(testValidator))
        assertThat(error, nullValue())
    }

    @Test
    fun testGet303Redirect() {
        val testValidator = "state:${Random().nextDouble()}"

        val firstRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mockResponse()
                .withHeader("Location", mockPath("get?validate=$testValidator"))
                .withStatusCode(HttpURLConnection.HTTP_SEE_OTHER)

        val secondRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mockChain(request = firstRequest, response = firstResponse)
        mockChain(request = secondRequest, response = mockReflect())

        val manager = FuelManager()
        val (_, _, result) = manager.request(Method.GET, mockPath("redirect")).responseString()
        val (data, error) = result

        assertThat(data, notNullValue())
        assertThat(data, containsString(testValidator))
        assertThat(error, nullValue())
    }

    @Test
    fun testGetNotModified() {
        val firstRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/not-modified")

        val firstResponse = mockResponse()
                .withStatusCode(HttpURLConnection.HTTP_NOT_MODIFIED)

        mockChain(request = firstRequest, response = firstResponse)
        val manager = FuelManager()
        val (_, _, result) = manager.request(Method.GET, mockPath("not-modified")).responseString()
        val (data, error) = result

        // TODO: Not Modified should not be an error
        assertThat(data, nullValue())
        assertThat(error, notNullValue())
    }

    @Test
    fun testGetRedirectNoUrl() {
        val firstRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mockResponse()
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val secondRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mockChain(request = firstRequest, response = firstResponse)
        mockChain(request = secondRequest, response = mockReflect())

        val manager = FuelManager()
        val (_, _, result) = manager.request(Method.GET, mockPath("redirect")).responseString()
        val (data, error) = result

        assertThat(data, nullValue())
        assertThat(error, notNullValue())
    }

    @Test
    fun testGetWrongUrl() {
        val firstRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mockResponse()
                .withHeader("Location", mockPath("not-found"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val secondRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/not-found")

        val secondResponse = mockResponse()
                .withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)

        mockChain(request = firstRequest, response = firstResponse)
        mockChain(request = secondRequest, response = secondResponse)

        val manager = FuelManager()
        val (_, _, result) = manager.request(Method.GET, mockPath("redirect")).responseString()
        val (data, error) = result

        assertThat(data, nullValue())
        assertThat(error, notNullValue())
    }

    @Test
    fun testPost301Redirect() {
        val firstRequest = mockRequest()
                .withMethod(Method.POST.value)
                .withPath("/redirect")

        val firstResponse = mockResponse()
                .withHeader("Location", mockPath("get"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_PERM)

        val secondRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mockChain(request = firstRequest, response = firstResponse)
        mockChain(request = secondRequest, response = mockReflect())

        val manager = FuelManager()
        val requests = mutableListOf<Request>()

        manager.addRequestInterceptor { next: (Request) -> Request ->
            { r: Request ->
                requests.add(r)
                next(r)
            }
        }

        val (originalRequest, _, result) = manager.request(Method.POST, mockPath("redirect")).responseString()
        val (data, error) = result

        assertThat(data, notNullValue())
        assertThat(error, nullValue())
        assertThat(originalRequest.method, isEqualTo(Method.POST))
        assertThat(requests[1].method, isEqualTo(Method.GET))
    }

    @Test
    fun testPost302Redirect() {
        val firstRequest = mockRequest()
                .withMethod(Method.POST.value)
                .withPath("/redirect")

        val firstResponse = mockResponse()
                .withHeader("Location", mockPath("get"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val secondRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mockChain(request = firstRequest, response = firstResponse)
        mockChain(request = secondRequest, response = mockReflect())

        val manager = FuelManager()
        val requests = mutableListOf<Request>()

        manager.addRequestInterceptor { next: (Request) -> Request ->
            { r: Request ->
                requests.add(r)
                next(r)
            }
        }

        val (originalRequest, _, result) = manager.request(Method.POST, mockPath("redirect")).responseString()
        val (data, error) = result

        assertThat(data, notNullValue())
        assertThat(error, nullValue())
        assertThat(originalRequest.method, isEqualTo(Method.POST))
        assertThat(requests[1].method, isEqualTo(Method.GET))
    }

    @Test
    fun testPost303Redirect() {
        val firstRequest = mockRequest()
                .withMethod(Method.POST.value)
                .withPath("/redirect")

        val firstResponse = mockResponse()
                .withHeader("Location", mockPath("get"))
                .withStatusCode(HttpURLConnection.HTTP_SEE_OTHER)

        val secondRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mockChain(request = firstRequest, response = firstResponse)
        mockChain(request = secondRequest, response = mockReflect())

        val manager = FuelManager()
        val requests = mutableListOf<Request>()

        manager.addRequestInterceptor { next: (Request) -> Request ->
            { r: Request ->
                requests.add(r)
                next(r)
            }
        }

        val (originalRequest, _, result) = manager.request(Method.POST, mockPath("redirect")).responseString()
        val (data, error) = result

        assertThat(data, notNullValue())
        assertThat(error, nullValue())
        assertThat(originalRequest.method, isEqualTo(Method.POST))
        assertThat(requests[1].method, isEqualTo(Method.GET))
    }

    @Test
    fun testPost307Redirect() {
        val firstRequest = mockRequest()
                .withMethod(Method.POST.value)
                .withPath("/redirect")

        val firstResponse = mockResponse()
                .withHeader("Location", mockPath("post"))
                .withStatusCode(307)

        val secondRequest = mockRequest()
                .withMethod(Method.POST.value)
                .withPath("/post")

        mockChain(request = firstRequest, response = firstResponse)
        mockChain(request = secondRequest, response = mockReflect())

        val manager = FuelManager()
        val requests = mutableListOf<Request>()

        manager.addRequestInterceptor { next: (Request) -> Request ->
            { r: Request ->
                requests.add(r)
                next(r)
            }
        }

        val (originalRequest, _, result) = manager.request(Method.POST, mockPath("redirect")).responseString()
        val (data, error) = result

        assertThat(data, notNullValue())
        assertThat(error, nullValue())
        assertThat(originalRequest.method, isEqualTo(Method.POST))
        assertThat(requests[1].method, isEqualTo(Method.POST))
    }

    @Test
    fun testPost308Redirect() {
        val firstRequest = mockRequest()
                .withMethod(Method.POST.value)
                .withPath("/redirect")

        val firstResponse = mockResponse()
                .withHeader("Location", mockPath("post"))
                .withStatusCode(308)

        val secondRequest = mockRequest()
                .withMethod(Method.POST.value)
                .withPath("/post")

        mockChain(request = firstRequest, response = firstResponse)
        mockChain(request = secondRequest, response = mockReflect())

        val manager = FuelManager()
        val requests = mutableListOf<Request>()

        manager.addRequestInterceptor { next: (Request) -> Request ->
            { r: Request ->
                requests.add(r)
                next(r)
            }
        }

        val (originalRequest, _, result) = manager.request(Method.POST, mockPath("redirect")).responseString()
        val (data, error) = result

        assertThat(data, notNullValue())
        assertThat(error, nullValue())
        assertThat(originalRequest.method, isEqualTo(Method.POST))
        assertThat(requests[1].method, isEqualTo(Method.POST))
    }

    @Test
    fun testHeaderIsPassingAlongWithRedirection() {
        val testValidator = "state:${Random().nextDouble()}"

        val firstRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mockResponse()
                .withHeader("Location", mockPath("get?validate=$testValidator"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val secondRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mockChain(request = firstRequest, response = firstResponse)
        mockChain(request = secondRequest, response = mockReflect())

        val manager = FuelManager()
        val (_, _, result) = manager.request(Method.GET, mockPath("redirect"))
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
        val firstRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mockResponse()
                .withHeader("Location", mockPath("basic-auth/user/pass"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val username = "user"
        val password = "pass"
        val auth = "$username:$password"
        val encodedAuth = auth.encodeBase64ToString()

        val secondRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/basic-auth/user/pass")
                .withHeader("Authorization", "Basic $encodedAuth")

        mockChain(request = firstRequest, response = firstResponse)
        mockChain(request = secondRequest, response = mockReflect())

        val manager = FuelManager()

        val (_, _, result) = manager.request(Method.GET, mockPath("redirect"))
                .header("Foo" to "bar")
                .authenticate(username, password)
                .responseString()

        val (data, error) = result
        assertThat(data, notNullValue())
        assertThat(error, nullValue())
    }

    @Test
    fun testHeaderAuthenticationWillBeRemoveIfRedirectToDifferentHost() {
        val firstRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mockResponse()
                .withHeader("Location", mockPath("basic-auth/user/pass").replace("localhost", "127.0.0.1"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val username = "user"
        val password = "pass"

        val secondRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/basic-auth/user/pass")

        mockChain(request = firstRequest, response = firstResponse)
        mockChain(request = secondRequest, response = mockReflect())

        val manager = FuelManager()

        val requests = mutableListOf<Request>()

        manager.addRequestInterceptor { next: (Request) -> Request ->
            { r: Request ->
                requests.add(r)
                next(r)
            }
        }

        val (_, _, result) = manager.request(Method.GET, mockPath("redirect"))
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
        val firstRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mockResponse()
                .withHeader("Location", mockPath("get"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        mockChain(request = firstRequest, response = firstResponse)

        val manager = FuelManager()
        val (_, _, result) = manager.request(Method.GET, mockPath("redirect"))
                .allowRedirects(false)
                .responseString()

        val (data, error) = result

        // TODO: This is current based on the current behavior, however we need to fix this as it should handle 100 - 399 gracefully not httpException
        assertThat(data, nullValue())
        assertThat(error, notNullValue())
    }

    @Test
    fun testRemoveAllRequestInterceptors() {
        val firstRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withPath("/teapot")

        val firstResponse = mockResponse()
                .withStatusCode(418)

        mockChain(request = firstRequest, response = firstResponse)

        val manager = FuelManager()
        manager.removeAllRequestInterceptors()

        val (request, response, result) = manager.request(Method.GET, mockPath("teapot")).responseString()

        val (data, error) = result
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, notNullValue())
        assertThat(data, nullValue())

        assertThat(response.statusCode, isEqualTo(418))
    }
}
