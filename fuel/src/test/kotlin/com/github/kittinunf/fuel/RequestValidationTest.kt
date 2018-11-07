package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.interceptors.validatorResponseInterceptor
import com.github.kittinunf.fuel.test.MockHttpTestCase
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Test
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RequestValidationTest : MockHttpTestCase() {
    @Test
    fun httpValidationWithDefaultCase() {
        // Register all valid
        for (status in (200..299)) {
            mock.chain(
                request = mock.request().withMethod(Method.GET.value).withPath("/$status"),
                response = mock.response().withStatusCode(status)
            )
        }

        // Register teapot
        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/418"),
            response = mock.response().withStatusCode(418)
        )

        // Test defaults
        for (status in (200..299)) {
            val (request, response, result) = FuelManager().request(Method.GET, mock.path("$status")).response()
            val (_, error) = result

            assertThat(request, notNullValue())
            assertThat(response, notNullValue())
            assertThat(error, nullValue())

            assertThat(response.statusCode, isEqualTo(status))
        }

        // Test invalid
        val (request, response, result) = FuelManager().request(Method.GET, mock.path("418")).response()
        val (_, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, notNullValue())
        assertThat(error!!.errorData, notNullValue())

        assertThat(response.statusCode, isEqualTo(418))
    }

    @Test
    fun httpValidationWithCustomValidCase() {
        val preDefinedStatusCode = 203

        val manager = FuelManager()

        manager.removeAllResponseInterceptors()
        manager.addResponseInterceptor(validatorResponseInterceptor(200..202))

        // Response to ANY GET request, with a 203 which should have been okay, but it's not with
        // the custom valid case
        mock.chain(
            request = mock.request().withMethod(Method.GET.value),
            response = mock.response().withStatusCode(203)
        )

        // this validate (200..202) which should fail with 203
        val (request, response, result) = manager.request(Method.GET, mock.path("any")).responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, notNullValue())
        assertThat(data, nullValue())

        assertThat(response.statusCode, isEqualTo(preDefinedStatusCode))
    }

    @Test
    fun httpValidationWithCustomInvalidCase() {
        val preDefinedStatusCode = 418
        val manager = FuelManager()

        manager.removeAllResponseInterceptors()
        manager.addResponseInterceptor(validatorResponseInterceptor(400..419))

        mock.chain(
            request = mock.request().withMethod(Method.GET.value),
            response = mock.response().withStatusCode(preDefinedStatusCode)
        )

        val (request, response, result) = manager.request(Method.GET, mock.path("status/$preDefinedStatusCode")).response()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.statusCode, isEqualTo(preDefinedStatusCode))
    }
}
