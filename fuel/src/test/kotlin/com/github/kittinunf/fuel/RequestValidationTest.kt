package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.getAs
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import org.hamcrest.CoreMatchers.`is` as isEqualTo

/**
 * Created by Kittinun Vantasin on 5/21/15.
 */

class RequestValidationTest : BaseTestCase() {

    val manager: Manager by lazy {
        Manager().apply {
            basePath = "http://httpbin.org"
        }
    }

    @Before
    fun setUp() {
        lock = CountDownLatch(1)
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

            lock.countDown()
        }

        await()

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

        //this validate (200..202) which should fail with 203
        manager.request(Method.GET, "/status/$preDefinedStatusCode").validate(200..202).responseString { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            data = d
            error = err

            lock.countDown()
        }

        await()

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

        manager.request(Method.GET, "/status/$preDefinedStatusCode").validate(400..419).response { req, res, result ->
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

            lock.countDown()
        }

        await()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(response?.httpStatusCode, isEqualTo(preDefinedStatusCode))
    }

}
