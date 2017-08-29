package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.util.FuelRouting
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Test
import java.net.HttpURLConnection
import org.hamcrest.CoreMatchers.`is` as isEqualTo

/**
 * Created by matteocrippa on 8/19/17.
 */
class RoutingTest: BaseTestCase() {
    sealed class TestApi: FuelRouting {

        override val basePath = "https://httpbin.org/"

        class getTest: TestApi()
        class getParamsTest(val name: String, val value: String): TestApi()

        override val method: Method
            get() {
                when(this) {
                    is getTest -> return Method.GET
                    is getParamsTest -> return Method.GET
                }
            }

        override val path: String
            get() {
                return when(this) {
                    is getTest -> "/get"
                    is getParamsTest -> "/get"
                }
            }

        override val params: List<Pair<String, Any?>>?
            get() {
                return when(this) {
                    is getParamsTest -> listOf(this.name to this.value)
                    else -> null
                }
            }

        override val headers: Map<String, String>?
            get() {
                return null
            }

    }

    @Test
    fun httpRouterGet() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        Fuel.request(TestApi.getTest()).responseString { req, res, result ->
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
        assertThat(response?.httpStatusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpRouterGetParams() {

        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        val paramKey = "foo"
        val paramValue = "bar"

        Fuel.request(TestApi.getParamsTest(name = paramKey, value = paramValue)).responseString { req, res, result ->
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
        assertThat(response?.httpStatusCode, isEqualTo(statusCode))

        assertThat(string, containsString(paramKey))
        assertThat(string, containsString(paramValue))
    }
}