package com.github.kittinunf.fuel.toolbox

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.test.MockHttpTestCase
import com.github.kittinunf.fuel.test.MockReflected
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockserver.matchers.Times
import org.mockserver.model.Header.header
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection

class HttpClientTest : MockHttpTestCase() {

    class TestHook : Client.Hook {
        override fun preConnect(connection: HttpURLConnection, request: Request) {
            // no-op
        }

        override fun interpretResponseStream(request: Request, inputStream: InputStream): InputStream = inputStream

        override fun postConnect(request: Request) {
            // no-op
        }

        override fun httpExchangeFailed(request: Request, exception: IOException) {
            // no-op
        }
    }

    @Test
    fun httpClientIsTheDefaultClient() {
        val request = Fuel.request(Method.GET, mock.path("default-client"))
        assertThat(request.executionOptions.client, instanceOf(HttpClient::class.java))
    }

    @Test
    fun usesOverrideMethodForPatch() {
        val request = Fuel.patch(mock.path("patch-with-override"))

        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/patch-with-override"),
            response = mock.reflect()
        )

        val (_, _, result) = request.responseObject(MockReflected.Deserializer())
        val (data, error) = result

        assertThat("Expected data, actual error $error", data, notNullValue())
        assertThat(data!!.method, equalTo(Method.POST.value))
        assertThat(data["X-HTTP-Method-Override"].firstOrNull(), equalTo(Method.PATCH.value))
    }

    @Test
    fun injectsAcceptTransferEncoding() {
        val request = reflectedRequest(Method.GET, "accept-transfer-encoding")
        val (_, _, result) = request.responseObject(MockReflected.Deserializer())
        val (data, error) = result

        assertThat("Expected data, actual error $error", data, notNullValue())
        assertThat(data!![Headers.ACCEPT_TRANSFER_ENCODING].size, not(equalTo(0)))
    }

    @Test
    fun setsContentLengthIfKnown() {
        val request = reflectedRequest(Method.POST, "content-length-test")
            .body("my-body")

        val (_, _, result) = request.responseObject(MockReflected.Deserializer())
        val (data, error) = result

        assertThat("Expected data, actual error $error", data, notNullValue())
        assertThat(data!![Headers.CONTENT_LENGTH].firstOrNull(), equalTo("my-body".toByteArray().size.toString()))
    }

    @Test
    fun dropBodyForGetRequest() {
        val request = reflectedRequest(Method.GET, "get-body-output")
            .body("my-body")

        val (_, _, result) = request.responseObject(MockReflected.Deserializer())
        val (data, error) = result

        assertThat("Expected data, actual error $error", data, notNullValue())
        assertThat(data!!.body, nullValue())
    }

    @Test
    fun allowPostWithBody() {
        val request = reflectedRequest(Method.POST, "post-body-output")
            .body("my-body")

        val (_, _, result) = request.responseObject(MockReflected.Deserializer())
        val (data, error) = result

        assertThat("Expected data, actual error $error", data, notNullValue())
        assertThat(data!!.body!!.string, equalTo("my-body"))
    }

    @Test
    fun allowPatchWithBody() {
        val request = Fuel.patch(mock.path("patch-body-output"))
            .body("my-body")

        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/patch-body-output"),
            response = mock.reflect()
        )

        val (_, _, result) = request.responseObject(MockReflected.Deserializer())
        val (data, error) = result

        assertThat("Expected data, actual error $error", data, notNullValue())
        assertThat(data!!.body!!.string, equalTo("my-body"))
    }

    @Test
    fun allowDeleteWithBody() {
        val request = reflectedRequest(Method.DELETE, "delete-body-output")
            .body("my-body")

        val (_, _, result) = request.responseObject(MockReflected.Deserializer())
        val (data, error) = result

        assertThat("Expected data, actual error $error", data, notNullValue())
        assertThat(data!!.body!!.string, equalTo("my-body"))
    }

    @Test
    fun canDisableClientCache() {
        mock.chain(
            request = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/cached"),
            response = mock.response()
                .withHeader(Headers.CACHE_CONTROL, "max-age=600")
                .withBody("cached"),
            times = Times.once()
        )

        mock.chain(
            request = mock.request()
                .withMethod(Method.GET.value)
                .withPath("/cached")
                .withHeader(header(Headers.CACHE_CONTROL, "no-cache")),
            response = mock.response()
                .withHeader(Headers.CACHE_CONTROL, "max-age=600")
                .withBody("fresh"),
            times = Times.once()
        )

        val request = Fuel.get(mock.path("cached"))
        val (_, _, result) = request.responseString()
        val (data, error) = result

        assertThat("Expected data, actual error $error", data, notNullValue())
        assertThat(data, equalTo("cached"))

        val (_, _, result2) = request.apply { executionOptions.useHttpCache = false }.responseString()
        val (data2, error2) = result2

        assertThat("Expected data, actual error $error2", data2, notNullValue())
        assertThat(data2, equalTo("fresh"))
    }

    @Test
    fun changeClientHook() {

        val request = Fuel.request(Method.GET, mock.path("change-hook")).apply {
            val httpClient = executionOptions.client as HttpClient
            httpClient.hook = TestHook()
        }

        val client = request.executionOptions.client as HttpClient
        assertThat(client.hook, instanceOf(TestHook::class.java))
    }
}
