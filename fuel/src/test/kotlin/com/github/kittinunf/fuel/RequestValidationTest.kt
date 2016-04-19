package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.fuel.core.interceptors.validatorResponseInterceptor
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.getAs
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Test
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RequestValidationTest : BaseTestCase() {

    val manager: FuelManager by lazy {
        FuelManager().apply {
            basePath = "http://httpbin.org"
        }
    }

    @Test
    fun httpValidationWithDefaultCase() {
        val preDefinedStatusCode = 418

        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        //this validate (200..299) which should fail with 418
        manager.request(Method.GET, "/status/$preDefinedStatusCode").response { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            data = d
            error = err
        }

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, notNullValue())
        assertThat(error?.errorData, notNullValue())
        assertThat(data, nullValue())

        assertThat(response?.httpStatusCode, isEqualTo(preDefinedStatusCode))
    }

    @Test
    fun httpValidationWithCustomValidCase() {
        val preDefinedStatusCode = 203

        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.removeAllResponseInterceptors()
        manager.addResponseInterceptor(validatorResponseInterceptor(200..202))

        //this validate (200..202) which should fail with 203
        manager.request(Method.GET, "/status/$preDefinedStatusCode").responseString { req, res, result ->
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

        assertThat(response?.httpStatusCode, isEqualTo(preDefinedStatusCode))
    }

    @Test
    fun httpValidationWithCustomInvalidCase() {
        val preDefinedStatusCode = 418

        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.removeAllResponseInterceptors()
        manager.addResponseInterceptor(validatorResponseInterceptor(400..419))

        manager.request(Method.GET, "/status/$preDefinedStatusCode").response { req, res, result ->
            request = req
            response = res

            when (result) {
                is Result.Failure -> {
                    error = result.getAs()
                }
                is Result.Success -> {
                    data = result.getAs()
                }
            }
        }

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response?.httpStatusCode, isEqualTo(preDefinedStatusCode))
    }

}
