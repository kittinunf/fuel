package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.util.encodeBase64ToString
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Test
import java.net.HttpURLConnection
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RequestAuthenticationTest : MockHttpTestCase() {
    private val user: String = "username"
    private val password: String = "password"

    @Test
    fun httpBasicAuthenticationWithInvalidCase() {
        val manager = FuelManager()
        val auth = "$user:$password"
        val encodedAuth = auth.encodeBase64ToString()

        val correctRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withHeader("Authorization", "Basic $encodedAuth")
                .withPath("/authenticate")

        val incorrectRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withHeader("Authorization")
                .withPath("/authenticate")

        val correctResponse = mockReflect()
        val incorrectResponse = mockResponse().withStatusCode(HttpURLConnection.HTTP_UNAUTHORIZED)

        mockChain(request = correctRequest, response = correctResponse)
        mockChain(request = incorrectRequest, response = incorrectResponse)

        val (request, response, result) = manager.request(Method.GET, mockPath("authenticate"))
                .authenticate("invalid", "authentication")
                .response()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, notNullValue())
        assertThat(data, nullValue())

        val statusCode = HttpURLConnection.HTTP_UNAUTHORIZED
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpBasicAuthenticationWithValidCase() {
        val manager = FuelManager()
        val auth = "$user:$password"
        val encodedAuth = auth.encodeBase64ToString()

        val correctRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withHeader("Authorization", "Basic $encodedAuth")
                .withPath("/authenticate")

        val incorrectRequest = mockRequest()
                .withMethod(Method.GET.value)
                .withHeader("Authorization")
                .withPath("/authenticate")

        val correctResponse = mockReflect()
        val incorrectResponse = mockResponse().withStatusCode(HttpURLConnection.HTTP_UNAUTHORIZED)

        mockChain(request = correctRequest, response = correctResponse)
        mockChain(request = incorrectRequest, response = incorrectResponse)

        val (request, response, result) = manager.request(Method.GET, mockPath("authenticate"))
                .authenticate(user, password)
                .response()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

}
