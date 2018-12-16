package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.HttpException
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.test.MockHttpTestCase
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.isA
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Test
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RequestValidationTest : MockHttpTestCase() {
    @Test
    fun httpValidationWithDefaultCase() {
        // Register all valid
        for (status in (200..399)) {
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

        // Register 501
        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/501"),
            response = mock.response().withStatusCode(501)
        )

        // Test defaults
        for (status in (200..399)) {
            val (request, response, result) = FuelManager().request(Method.GET, mock.path("$status")).response()
            val (_, error) = result

            assertThat(request, notNullValue())
            assertThat(response, notNullValue())
            assertThat(error, nullValue())

            assertThat(response.statusCode, isEqualTo(status))
        }

        // Test invalid 4xx
        val (request, response, result) =
                FuelManager().request(Method.GET, mock.path("418")).response()
        val (_, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, notNullValue())
        assertThat(error!!.errorData, notNullValue())

        assertThat(response.statusCode, isEqualTo(418))

        // Test invalid 5xx
        val (anotherRequest, anotherResponse, anotherResult) =
                FuelManager().request(Method.GET, mock.path("501")).response()
        val (_, anotherError) = result

        assertThat(anotherRequest, notNullValue())
        assertThat(anotherResponse, notNullValue())
        assertThat(anotherError, notNullValue())
        assertThat(anotherError!!.errorData, notNullValue())

        assertThat(anotherResponse.statusCode, isEqualTo(501))
    }

    @Test
    fun httpValidationWithCustomValidCase() {
        val preDefinedStatusCode = 203

        val manager = FuelManager()

        // Response to ANY GET request, with a 203 which should have been okay, but it's not with
        // the custom valid case
        mock.chain(
            request = mock.request().withMethod(Method.GET.value),
            response = mock.response().withStatusCode(203)
        )

        // this validate (200..202) which should fail with 203
        val (request, response, result) =
                manager.request(Method.GET, mock.path("any"))
                        .validate { it.statusCode in (200..202) }
                        .responseString()

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

        mock.chain(
            request = mock.request().withMethod(Method.GET.value),
            response = mock.response().withStatusCode(preDefinedStatusCode)
        )

        val (request, response, result) =
                manager.request(Method.GET, mock.path("status/$preDefinedStatusCode"))
                        .validate { it.statusCode in (400..419) }
                        .response()

        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response.statusCode, isEqualTo(preDefinedStatusCode))
    }

    @Test
    fun httpAnyFailureWithCustomValidator() {
        val manager = FuelManager()

        mock.chain(
                request = mock.request().withMethod(Method.GET.value),
                response = mock.response().withStatusCode(200)
        )

        val (request, response, result) =
                manager.request(Method.GET, mock.path("any"))
                        .validate { false } // always fail
                        .responseString()

        val (_, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, notNullValue())
        assertThat(error!!.exception as HttpException, isA(HttpException::class.java))
        assertThat(error.exception.message, containsString("HTTP Exception 200"))
    }
}
