package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.HeaderValues
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.util.FuelRouting
import com.github.kittinunf.fuel.util.decodeBase64ToString
import com.github.kittinunf.fuel.util.encodeBase64
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.json.JSONObject
import org.junit.Assert.assertThat
import org.junit.Test
import java.net.HttpURLConnection
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RoutingTest : MockHttpTestCase() {
    private val manager: FuelManager by lazy { FuelManager() }
    sealed class TestApi(private val host: String) : FuelRouting {
        override val basePath = this.host

        class GetTest(host: String) : TestApi(host)
        class GetParamsTest(host: String, val name: String, val value: String) : TestApi(host)
        class PostBodyTest(host: String, val value: String) : TestApi(host)
        class PostBinaryBodyTest(host: String, val value: String) : TestApi(host)
        class PostEmptyBodyTest(host: String) : TestApi(host)

        override val method: Method
            get() {
                return when (this) {
                    is GetTest -> Method.GET
                    is GetParamsTest -> Method.GET
                    is PostBodyTest -> Method.POST
                    is PostBinaryBodyTest -> Method.POST
                    is PostEmptyBodyTest -> Method.POST
                }
            }

        override val path: String
            get() {
                return when (this) {
                    is GetTest -> "/get"
                    is GetParamsTest -> "/get"
                    is PostBodyTest -> "/post"
                    is PostBinaryBodyTest -> "/post"
                    is PostEmptyBodyTest -> "/post"
                }
            }

        override val params: List<Pair<String, Any?>>?
            get() {
                return when (this) {
                    is GetParamsTest -> listOf(this.name to this.value)
                    else -> null
                }
            }

        override val bytes: ByteArray?
            get() {
                return when (this) {
                    is PostBinaryBodyTest -> {
                        val json = JSONObject()
                        json.put("id", this.value)
                        json.toString().toByteArray().encodeBase64()
                    }
                    else -> null
                }
            }

        override val body: String?
            get() {
                return when (this) {
                    is PostBodyTest -> {
                        val json = JSONObject()
                        json.put("id", this.value)
                        json.toString()
                    }
                    else -> null
                }
            }

        override val headers: Map<String, HeaderValues>?
            get() {
                return when (this) {
                    is PostBodyTest -> mapOf("Content-Type" to listOf("application/json"))
                    is PostBinaryBodyTest -> mapOf("Content-Type" to listOf("application/octet-stream"))
                    is PostEmptyBodyTest -> mapOf("Content-Type" to listOf("application/json"))
                    else -> null
                }
            }
    }

    @Test
    fun httpRouterGet() {
        mock.chain(
            request = mock.request().withMethod(Method.GET.value),
            response = mock.reflect()
        )

        val (request, response, result) = manager.request(TestApi.GetTest(mock.path(""))).responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpRouterGetParams() {
        mock.chain(
            request = mock.request().withMethod(Method.GET.value),
            response = mock.reflect()
        )

        val paramKey = "foo"
        val paramValue = "bar"

        val (request, response, result) = manager.request(TestApi.GetParamsTest(host = mock.path(""), name = paramKey, value = paramValue)).responseString()
        val (data, error) = result

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        assertThat(string, containsString(paramKey))
        assertThat(string, containsString(paramValue))
    }

    @Test
    fun httpRouterPostBody() {
        mock.chain(
            request = mock.request().withMethod(Method.POST.value),
            response = mock.reflect()
        )

        val paramValue = "42"

        val (request, response, result) = manager.request(TestApi.PostBodyTest(mock.path(""), paramValue)).responseString()
        val (data, error) = result

        val string = JSONObject(data).getJSONObject("body").getString("string")

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        val res = JSONObject(string)
        assertThat(res.getString("id"), isEqualTo(paramValue))
    }

    @Test
    fun httpRouterPostBinaryBody() {
        mock.chain(
            request = mock.request().withMethod(Method.POST.value),
            response = mock.reflect()
        )
        val paramValue = "42"

        val (request, response, result) = manager.request(TestApi.PostBinaryBodyTest(mock.path(""), paramValue)).responseString()
        val (data, error) = result

        // Binary data is encoded in base64 by mock server
        val string = JSONObject(data).getJSONObject("body").getString("base64Bytes").decodeBase64ToString()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        val bytes = string!!.decodeBase64ToString()
        assertThat(bytes, containsString(paramValue))
    }

    @Test
    fun httpRouterPostEmptyBody() {
        mock.chain(
            request = mock.request().withMethod(Method.POST.value),
            response = mock.reflect()
        )

        val (request, response, result) = manager.request(TestApi.PostEmptyBodyTest(mock.path(""))).responseString()
        val (data, error) = result

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        val res = JSONObject(string)
        assertThat(res.optString("data"), isEqualTo(""))
    }
}
