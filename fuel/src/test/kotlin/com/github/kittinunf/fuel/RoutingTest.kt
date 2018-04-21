package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.util.FuelRouting
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
        class GetBodyTest(val value: String) : TestApi()

        override val method: Method
            get() {
                return when (this) {
                    is GetTest -> Method.GET
                    is GetParamsTest -> Method.GET
                    is GetBodyTest -> Method.POST
                }
            }

        override val path: String
            get() {
                return when (this) {
                    is GetTest -> "/get"
                    is GetParamsTest -> "/get"
                    is GetBodyTest -> "/post"
                }
            }

        override val params: List<Pair<String, Any?>>?
            get() {
                return when (this) {
                    is GetParamsTest -> listOf(this.name to this.value)
                    else -> null
                }
            }

        override val body: String?
            get() {
                return when (this) {
                    is GetParamsTest -> {
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
                    is GetParamsTest -> mapOf("Content-Type" to "application/json")
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
    fun httpRouterGetBody() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        val paramValue = "42"

        manager.request(TestApi.GetBodyTest(paramValue)).responseString { req, res, result ->
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

        assertThat(string, containsString("42"))
    }
}