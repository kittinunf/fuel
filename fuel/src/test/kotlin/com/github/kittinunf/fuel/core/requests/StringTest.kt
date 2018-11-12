package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Handler
import com.github.kittinunf.fuel.core.HttpException
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.ResponseHandler
import com.github.kittinunf.fuel.test.MockHttpTestCase
import com.github.kittinunf.result.Result
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.isA
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Assert.fail
import org.junit.Test
import java.net.HttpURLConnection
import java.util.UUID

class StringTest : MockHttpTestCase() {

    private fun randomString() = UUID.randomUUID().toString()
    private fun getString(string: String, path: String = "string"): Request {
        mock.chain(
            request = mock.request().withPath("/$path"),
            response = mock.response().withBody(string)
        )

        return Fuel.request(Method.GET, mock.path(path))
    }

    private fun mocked404(method: Method = Method.GET, path: String = "invalid/url"): Request {
        mock.chain(
            request = mock.request().withPath("/$path"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )
        return Fuel.request(method, mock.path(path))
    }

    @Test
    fun response() {
        val string = randomString()
        val (request, response, result) = getString(string).responseString()
        val (data, error) = result
        assertThat("Expected data, actual error $error", data, notNullValue())

        assertThat(data, equalTo(string))
        assertThat("Expected request to be not null", request, notNullValue())
        assertThat("Expected response to be not null", response, notNullValue())
    }

    @Test
    fun responseFailure() {
        val (_, _, result) = mocked404().responseString()
        val (data, error) = result

        assertThat("Expected error, actual data $data", error, notNullValue())
    }

    @Test
    fun responseHandler() {
        val string = randomString()
        val running = getString(string).responseString(object : Handler<String> {
            override fun success(value: String) {
                assertThat(value, equalTo(string))
            }

            override fun failure(error: FuelError) {
                fail("Expected data, actual error $error")
            }
        })

        running.join()
    }

    @Test
    fun responseHandlerFailure() {
        val running = mocked404().responseString(object : Handler<String> {
            override fun success(value: String) {
                fail("Expected error, actual data $value")
            }

            override fun failure(error: FuelError) {
                assertThat(error, notNullValue())
                assertThat(error.exception as? HttpException, isA(HttpException::class.java))
            }
        })

        running.join()
    }

    @Test
    fun responseResponseHandler() {
        val string = randomString()
        val running = getString(string).responseString(object : ResponseHandler<String> {
            override fun success(request: Request, response: Response, value: String) {
                assertThat("Expected data to be not null", value, notNullValue())
                assertThat(value, equalTo(string))

                assertThat("Expected request to be not null", request, notNullValue())
                assertThat("Expected response to be not null", response, notNullValue())
            }

            override fun failure(request: Request, response: Response, error: FuelError) {
                fail("Expected data, actual error $error")
            }
        })

        running.join()
    }

    @Test
    fun responseResponseHandlerFailure() {
        val running = mocked404().responseString(object : ResponseHandler<String> {
            override fun success(request: Request, response: Response, value: String) {
                fail("Expected error, actual data $value")
            }

            override fun failure(request: Request, response: Response, error: FuelError) {
                assertThat(error, notNullValue())
                assertThat(error.exception as? HttpException, isA(HttpException::class.java))

                assertThat("Expected request to be not null", request, notNullValue())
                assertThat("Expected response to be not null", response, notNullValue())
            }
        })

        running.join()
    }

    @Test
    fun responseResultHandler() {
        val string = randomString()
        val running = getString(string).responseString { result: Result<String, FuelError> ->
            val (data, error) = result
            assertThat("Expected data, actual error $error", data, notNullValue())
            assertThat(data, equalTo(string))
        }

        running.join()
    }

    @Test
    fun responseResultHandlerFailure() {
        val running = mocked404().responseString { result: Result<String, FuelError> ->
            val (data, error) = result
            assertThat("Expected error, actual data $data", error, notNullValue())
        }

        running.join()
    }

    @Test
    fun responseResponseResultHandler() {
        val string = randomString()
        val running = getString(string).responseString { request, response, result ->
            val (data, error) = result
            assertThat("Expected data, actual error $error", data, notNullValue())
            assertThat(data, equalTo(string))

            assertThat("Expected request to be not null", request, notNullValue())
            assertThat("Expected response to be not null", response, notNullValue())
        }

        running.join()
    }

    @Test
    fun responseResponseResultHandlerFailure() {
        val running = mocked404().responseString { request, response, result ->
            val (data, error) = result
            assertThat("Expected error, actual data $data", error, notNullValue())
            assertThat(error!!.exception as? HttpException, isA(HttpException::class.java))

            assertThat("Expected request to be not null", request, notNullValue())
            assertThat("Expected response to be not null", response, notNullValue())
        }

        running.join()
    }
}
