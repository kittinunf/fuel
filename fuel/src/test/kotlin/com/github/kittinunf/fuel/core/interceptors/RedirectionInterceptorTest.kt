package com.github.kittinunf.fuel.core.interceptors

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.test.MockHttpTestCase
import com.github.kittinunf.fuel.test.MockReflected
import com.github.kittinunf.fuel.util.encodeBase64ToString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Test
import java.net.HttpURLConnection
import java.util.Random
import java.util.UUID
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RedirectionInterceptorTest : MockHttpTestCase() {

    private fun expectRedirectedUserAgent(baseRequest: Request): MockReflected {
        val randomUserAgent = "Fuel ${UUID.randomUUID()}"
        val (request, response, result) = baseRequest
                .header(Headers.USER_AGENT to randomUserAgent)
                .responseObject(MockReflected.Deserializer())

        val (data, error) = result
        assertThat("Expected data, actual error $error [${error?.stackTrace?.joinToString("\n")}]", data, notNullValue())

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data!!.userAgent, equalTo(randomUserAgent))
        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))

        return data
    }

    private fun expectNotRedirected(baseRequest: Request, status: Int = HttpURLConnection.HTTP_MOVED_TEMP) {
        val randomUserAgent = "Fuel ${UUID.randomUUID()}"
        val (request, response, result) = baseRequest
                .header(Headers.USER_AGENT to randomUserAgent)
                .response()

        val (data, error) = result

        assertThat(data, notNullValue())
        assertThat(error, nullValue())
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(response.statusCode, isEqualTo(status))
    }

    @Test
    fun followRedirectsViaLocation() {
        val firstRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader(Headers.LOCATION, mock.path("redirected"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val redirectedRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirected")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = redirectedRequest, response = mock.reflect())

        expectRedirectedUserAgent(FuelManager().request(Method.GET, mock.path("redirect")))
    }

    @Test
    fun followRedirectsViaContentLocation() {
        val firstRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader(Headers.CONTENT_LOCATION, mock.path("redirected"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val redirectedRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirected")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = redirectedRequest, response = mock.reflect())

        expectRedirectedUserAgent(FuelManager().request(Method.GET, mock.path("redirect")))
    }

    @Test
    fun doNotFollowEmptyRedirects() {
        val firstRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader(Headers.LOCATION, "")
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        mock.chain(request = firstRequest, response = firstResponse)
        expectNotRedirected(FuelManager().request(Method.GET, mock.path("redirect")), HttpURLConnection.HTTP_MOVED_TEMP)
    }

    @Test
    fun followRelativeRedirect() {
        val firstRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader(Headers.LOCATION, "/redirected")
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val redirectedRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirected")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = redirectedRequest, response = mock.reflect())

        expectRedirectedUserAgent(FuelManager().request(Method.GET, mock.path("redirect")))
    }

    @Test
    fun preserveRequestHeadersWithRedirects() {
        val firstRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader(Headers.LOCATION, "/redirected")
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val redirectedRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirected")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = redirectedRequest, response = mock.reflect())

        val manager = FuelManager()
        manager.addRequestInterceptor(LogRequestAsCurlInterceptor)

        val data = expectRedirectedUserAgent(
                FuelManager()
                        .request(Method.GET, mock.path("redirect"))
                        .header("Custom-Header" to "Fuel")
        )

        assertThat(data.headers["Custom-Header"].lastOrNull(), equalTo("Fuel"))
    }

    @Test
    fun preserveBaseHeadersWithRedirects() {
        val firstRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader(Headers.LOCATION, "/redirected")
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val redirectedRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirected")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = redirectedRequest, response = mock.reflect())

        val manager = FuelManager()
        manager.baseHeaders = mapOf("Custom-Header" to "Fuel")

        val data = expectRedirectedUserAgent(manager.request(Method.GET, mock.path("redirect")))
        assertThat(data.headers["Custom-Header"].lastOrNull(), equalTo("Fuel"))
    }

    @Test
    fun followMultipleRedirects() {
        val firstRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader(Headers.LOCATION, mock.path("intermediary"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val secondRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/intermediary")

        val secondResponse = mock.response()
                .withHeader(Headers.CONTENT_LOCATION, mock.path("redirected"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val redirectedRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirected")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = secondRequest, response = secondResponse)
        mock.chain(request = redirectedRequest, response = mock.reflect())

        expectRedirectedUserAgent(FuelManager().request(Method.GET, mock.path("redirect")))
    }

    @Test
    fun followRedirectToNotFound() {
        val firstRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader(Headers.LOCATION, mock.path("not-found"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val secondRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/not-found")

        val secondResponse = mock.response()
                .withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = secondRequest, response = secondResponse)

        val (request, response, result) = FuelManager().request(Method.GET, mock.path("redirect")).response()
        val (data, error) = result

        assertThat(data, nullValue())
        assertThat(error, notNullValue())
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_NOT_FOUND))
    }

    @Test
    fun getWithMovedPermanently() {
        val testValidator = "${Random().nextDouble()}"

        val firstRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader(Headers.LOCATION, mock.path("get?validate=$testValidator"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_PERM)

        val secondRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = secondRequest, response = mock.reflect())

        val data = expectRedirectedUserAgent(FuelManager().request(Method.GET, mock.path("redirect")))

        assertThat("Expected query to contains validate=\"$testValidator\", actual ${data.query}",
                data.query.any { it -> it.first == "validate" && (it.second as List<*>).first() == testValidator },
                equalTo(true)
        )
    }

    @Test
    fun getWithMovedTemporarily() {
        val testValidator = "${Random().nextDouble()}"

        val firstRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader(Headers.LOCATION, mock.path("get?validate=$testValidator"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val secondRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = secondRequest, response = mock.reflect())

        val data = expectRedirectedUserAgent(FuelManager().request(Method.GET, mock.path("redirect")))

        assertThat("Expected query to contains validate=\"$testValidator\", actual ${data.query}",
                data.query.any { it -> it.first == "validate" && (it.second as List<*>).first() == testValidator },
                equalTo(true)
        )
    }

    @Test
    fun getWithSeeOther() {
        val testValidator = "${Random().nextDouble()}"

        val firstRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader(Headers.LOCATION, mock.path("get?validate=$testValidator"))
                .withStatusCode(HttpURLConnection.HTTP_SEE_OTHER)

        val secondRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = secondRequest, response = mock.reflect())

        val data = expectRedirectedUserAgent(FuelManager().request(Method.GET, mock.path("redirect")))

        assertThat("Expected query to contains validate=\"$testValidator\", actual ${data.query}",
                data.query.any { it -> it.first == "validate" && (it.second as List<*>).first() == testValidator },
                equalTo(true)
        )
    }

    @Test
    fun getWithNotModified() {
        val firstRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/not-modified")

        val firstResponse = mock.response()
                .withStatusCode(HttpURLConnection.HTTP_NOT_MODIFIED)

        mock.chain(request = firstRequest, response = firstResponse)

        expectNotRedirected(
                FuelManager().request(Method.GET, mock.path("not-modified")),
                HttpURLConnection.HTTP_NOT_MODIFIED
        )
    }

    @Test
    fun doNotFollowRedirectWithMissingLocation() {
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

        assertThat(data, notNullValue())
        assertThat(error, nullValue())
    }

    @Test
    fun postWithMovedPermanently() {
        val testValidator = "${Random().nextDouble()}"
        val firstRequest = mock.request()
                .withMethod(Method.POST.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader(Headers.LOCATION, mock.path("get?validate=$testValidator"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_PERM)

        val secondRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = secondRequest, response = mock.reflect())

        val data = expectRedirectedUserAgent(FuelManager().request(Method.POST, mock.path("redirect")))
        assertThat("Expected query to contains validate=\"$testValidator\", actual ${data.query}",
                data.query.any { it -> it.first == "validate" && (it.second as List<*>).first() == testValidator },
                equalTo(true)
        )
    }

    @Test
    fun postWithMovedTemporarily() {
        val testValidator = "${Random().nextDouble()}"
        val firstRequest = mock.request()
                .withMethod(Method.POST.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader(Headers.LOCATION, mock.path("get?validate=$testValidator"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val secondRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = secondRequest, response = mock.reflect())

        val data = expectRedirectedUserAgent(FuelManager().request(Method.POST, mock.path("redirect")))
        assertThat("Expected query to contains validate=\"$testValidator\", actual ${data.query}",
                data.query.any { it -> it.first == "validate" && (it.second as List<*>).first() == testValidator },
                equalTo(true)
        )
    }

    @Test
    fun postWithSeeOther() {
        val testValidator = "${Random().nextDouble()}"
        val firstRequest = mock.request()
                .withMethod(Method.POST.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader(Headers.LOCATION, mock.path("get?validate=$testValidator"))
                .withStatusCode(HttpURLConnection.HTTP_SEE_OTHER)

        val secondRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/get")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = secondRequest, response = mock.reflect())

        val data = expectRedirectedUserAgent(FuelManager().request(Method.POST, mock.path("redirect")))
        assertThat("Expected query to contains validate=\"$testValidator\", actual ${data.query}",
                data.query.any { it -> it.first == "validate" && (it.second as List<*>).first() == testValidator },
                equalTo(true)
        )
    }

    @Test
    fun postWithTemporaryRedirect() {
        val testValidator = "${Random().nextDouble()}"
        val firstRequest = mock.request()
                .withMethod(Method.POST.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader(Headers.LOCATION, mock.path("post?validate=$testValidator"))
                .withStatusCode(307)

        val secondRequest = mock.request()
                .withMethod(Method.POST.value)
                .withPath("/post")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = secondRequest, response = mock.reflect())

        val data = expectRedirectedUserAgent(FuelManager().request(Method.POST, mock.path("redirect")))
        assertThat("Expected query to contains validate=\"$testValidator\", actual ${data.query}",
                data.query.any { it -> it.first == "validate" && (it.second as List<*>).first() == testValidator },
                equalTo(true)
        )
    }

    @Test
    fun postWithPermanentRedirect() {
        val testValidator = "${Random().nextDouble()}"
        val firstRequest = mock.request()
                .withMethod(Method.POST.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader(Headers.LOCATION, mock.path("post?validate=$testValidator"))
                .withStatusCode(308)

        val secondRequest = mock.request()
                .withMethod(Method.POST.value)
                .withPath("/post")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = secondRequest, response = mock.reflect())

        val data = expectRedirectedUserAgent(FuelManager().request(Method.POST, mock.path("redirect")))
        assertThat("Expected query to contains validate=\"$testValidator\", actual ${data.query}",
                data.query.any { it -> it.first == "validate" && (it.second as List<*>).first() == testValidator },
                equalTo(true)
        )
    }

    @Test
    fun authenticationForwardToSameHost() {
        val firstRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader(Headers.LOCATION, mock.path("basic-auth/user/pass"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val username = UUID.randomUUID().toString()
        val password = UUID.randomUUID().toString()
        val auth = "$username:$password"
        val encodedAuth = auth.encodeBase64ToString()

        val secondRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/basic-auth/user/pass")
                .withHeader(Headers.AUTHORIZATION, "Basic $encodedAuth")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = secondRequest, response = mock.reflect())

        expectRedirectedUserAgent(
                FuelManager().request(Method.GET, mock.path("redirect"))
                        .authentication()
                        .basic(username, password)
        )
    }

    @Test
    fun authenticationStrippedToDifferentHost() {
        val firstRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader(Headers.LOCATION, mock.path("basic-auth/user/pass").replace("localhost", "127.0.0.1"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        val username = UUID.randomUUID().toString()
        val password = UUID.randomUUID().toString()

        val secondRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/basic-auth/user/pass")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = secondRequest, response = mock.reflect())

        val data = expectRedirectedUserAgent(
                FuelManager().request(Method.GET, mock.path("redirect"))
                        .authentication()
                        .basic(username, password)
        )

        println(data)
        println(data)
    }

    @Test
    fun doNotFollowRedirectsViaRequest() {
        val firstRequest = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader(Headers.LOCATION, mock.path("get"))
                .withStatusCode(HttpURLConnection.HTTP_MOVED_TEMP)

        mock.chain(request = firstRequest, response = firstResponse)

        expectNotRedirected(
                FuelManager().request(Method.GET, mock.path("redirect"))
                        .allowRedirects(false),
                HttpURLConnection.HTTP_MOVED_TEMP
        )
    }

    @Test
    fun repeatableBodiesAreForwardedIfNotGet() {
        val testValidator = "${Random().nextDouble()}"
        val firstRequest = mock.request()
                .withMethod(Method.POST.value)
                .withPath("/redirect")

        val firstResponse = mock.response()
                .withHeader(Headers.LOCATION, mock.path("post?validate=$testValidator"))
                .withStatusCode(308)

        val secondRequest = mock.request()
                .withMethod(Method.POST.value)
                .withPath("/post")

        mock.chain(request = firstRequest, response = firstResponse)
        mock.chain(request = secondRequest, response = mock.reflect())

        val data = expectRedirectedUserAgent(FuelManager().request(Method.POST, mock.path("redirect")).body("body"))
        assertThat("Expected query to contains validate=\"$testValidator\", actual ${data.query}",
                data.query.any { it -> it.first == "validate" && (it.second as List<*>).first() == testValidator },
                equalTo(true)
        )
        assertThat("Expected body to be forwarded", data.body!!.string, equalTo("body"))
    }
}
