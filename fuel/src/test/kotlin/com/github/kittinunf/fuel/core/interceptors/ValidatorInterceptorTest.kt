package com.github.kittinunf.fuel.core.interceptors

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.test.MockHttpTestCase
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Test
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class ValidatorInterceptorTest : MockHttpTestCase() {
    @Test
    fun outsideOfValidatorRange() {
        val firstRequest = mock.request()
            .withMethod(Method.GET.value)
            .withPath("/invalid")

        val firstResponse = mock.response()
            .withStatusCode(418) // I'm a teapot

        mock.chain(request = firstRequest, response = firstResponse)

        val manager = FuelManager()
            .removeAllResponseInterceptors()
            .addResponseInterceptor(validatorResponseInterceptor(200..299))

        val (request, response, result) = manager.request(Method.GET, mock.path("invalid"))
            .responseString(Charsets.UTF_8)

        val (data, error) = result
        assertThat("Expected error, actual $data", error, notNullValue())
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())

        assertThat(response.statusCode, isEqualTo(418))
    }

    @Test
    fun insideOfValidatorRange() {
        val firstRequest = mock.request()
            .withMethod(Method.GET.value)
            .withPath("/invalid")

        val firstResponse = mock.response()
            .withStatusCode(418) // I'm a teapot

        mock.chain(request = firstRequest, response = firstResponse)

        val manager = FuelManager()
            .removeAllResponseInterceptors()
            .addResponseInterceptor(validatorResponseInterceptor(418..418))

        val (request, response, result) = manager.request(Method.GET, mock.path("invalid"))
            .responseString(Charsets.UTF_8)

        val (data, error) = result
        assertThat("Expected data, actual error $error", data, notNullValue())
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())

        assertThat(response.statusCode, isEqualTo(418))
    }
}
