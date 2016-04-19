package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.*
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Test
import java.net.HttpURLConnection
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RequestAuthenticationTest : BaseTestCase() {

    val user: String
    val password: String

    init {
        user = "username"
        password = "password"
    }

    val manager: FuelManager by lazy {
        FuelManager().apply {
            basePath = "http://httpbin.org"
        }
    }

    @Test
    fun httpBasicAuthenticationWithInvalidCase() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.request(Method.GET, "/basic-auth/$user/$password").authenticate("invalid", "authentication").response { req, res, result ->
            request = req
            response = res
            val (d, err) = result
            data = d
            error = err
        }

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, notNullValue())
        assertThat(data, nullValue())

        val statusCode = HttpURLConnection.HTTP_UNAUTHORIZED
        assertThat(response?.httpStatusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpBasicAuthenticationWithValidCase() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.request(Method.GET, "/basic-auth/$user/$password").authenticate(user, password).response { req, res, result ->
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

}
