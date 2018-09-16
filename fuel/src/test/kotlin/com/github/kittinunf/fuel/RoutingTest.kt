package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.util.FuelRouting
import com.github.kittinunf.fuel.util.decodeBase64
import com.github.kittinunf.fuel.util.encodeBase64
import org.hamcrest.CoreMatchers.*
import org.json.JSONObject
import org.junit.Assert.assertThat
import org.junit.Test
import java.net.HttpURLConnection
import org.hamcrest.CoreMatchers.`is` as isEqualTo

/**
 * Created by matteocrippa on 8/19/17.
 */
class RoutingTest: BaseTestCase() {
    private val manager: FuelManager by lazy { FuelManager() }
    sealed class TestApi: FuelRouting {

        override val basePath = "https://httpbin.org/"

        class GetTest : TestApi()
        class GetParamsTest(val name: String, val value: String) : TestApi()
        class PostBodyTest(val value: String) : TestApi()
        class PostBinaryBodyTest(val value: String) : TestApi()
        class PostEmptyBodyTest() : TestApi()

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

        override val headers: Map<String, String>?
            get() {
                return when (this) {
                    is PostBodyTest -> mapOf("Content-Type" to "application/json")
                    is PostBinaryBodyTest -> mapOf("Content-Type" to "application/octet-stream")
                    is PostEmptyBodyTest -> mapOf("Content-Type" to "application/json")
                    else -> null
                }
            }

    }

    @Test
    fun httpRouterGet() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.request(TestApi.GetTest()).responseString { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            data = d
            error = err
        }

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpRouterGetParams() {

        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        val paramKey = "foo"
        val paramValue = "bar"

        manager.request(TestApi.GetParamsTest(name = paramKey, value = paramValue)).responseString { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            data = d
            error = err
        }

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))

        assertThat(string, containsString(paramKey))
        assertThat(string, containsString(paramValue))
    }

    @Test
    fun httpRouterPostBody() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        val paramValue = "42"

        manager.request(TestApi.PostBodyTest(paramValue)).responseString { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            data = d
            error = err
        }

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))

        val res = JSONObject(string)
        assertThat(res.optString("data"), containsString(paramValue))
        assertThat(res.optString("json"), containsString(paramValue))
    }

    @Test
    fun httpRouterPostBinaryBody() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        val paramValue = "42"

        manager.request(TestApi.PostBinaryBodyTest(paramValue)).responseString { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            data = d
            error = err
        }

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))

        val res = JSONObject(string)
        val bytes = res.optString("data").decodeBase64()
        assertThat(bytes, containsString(paramValue))
        assertThat(res.optString("json"), not(containsString(paramValue)))
    }

    @Test
    fun httpRouterPostEmptyBody() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.request(TestApi.PostEmptyBodyTest()).responseString { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            data = d
            error = err
        }

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))

        val res = JSONObject(string)
        assertThat(res.optString("data"), isEqualTo(""))
    }
}
