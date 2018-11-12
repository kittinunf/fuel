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
import java.util.Random

class ByteArrayTest : MockHttpTestCase() {

    private fun randomBytes(n: Int = 255) = ByteArray(n).also { Random().nextBytes(it) }
    private fun getBytes(bytes: ByteArray, path: String = "bytes"): Request {
        mock.chain(
            request = mock.request().withPath("/$path"),
            response = mock.response().withBody(bytes)
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
        val bytes = randomBytes()
        val (request, response, result) = getBytes(bytes).response()
        val (data, error) = result
        assertThat("Expected data, actual error $error", data, notNullValue())

        assertThat(data, equalTo(bytes))
        assertThat("Expected request to be not null", request, notNullValue())
        assertThat("Expected response to be not null", response, notNullValue())
    }

    @Test
    fun responseFailure() {
        val (_, _, result) = mocked404().response()
        val (data, error) = result

        assertThat("Expected error, actual data $data", error, notNullValue())
    }

    @Test
    fun responseHandler() {
        val bytes = randomBytes()
        val running = getBytes(bytes).response(object : Handler<ByteArray> {
            override fun success(value: ByteArray) {
                assertThat(value, equalTo(bytes))
            }

            override fun failure(error: FuelError) {
                fail("Expected data, actual error $error")
            }
        })

        running.join()
    }

    @Test
    fun responseHandlerFailure() {
        val running = mocked404().response(object : Handler<ByteArray> {
            override fun success(value: ByteArray) {
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
        val bytes = randomBytes()
        val running = getBytes(bytes).response(object : ResponseHandler<ByteArray> {
            override fun success(request: Request, response: Response, value: ByteArray) {
                assertThat("Expected data to be not null", value, notNullValue())
                assertThat(value, equalTo(bytes))

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
        val running = mocked404().response(object : ResponseHandler<ByteArray> {
            override fun success(request: Request, response: Response, value: ByteArray) {
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
        val bytes = randomBytes()
        val running = getBytes(bytes).response { result: Result<ByteArray, FuelError> ->
            val (data, error) = result
            assertThat("Expected data, actual error $error", data, notNullValue())
            assertThat(data, equalTo(bytes))
        }

        running.join()
    }

    @Test
    fun responseResultHandlerFailure() {
        val running = mocked404().response { result: Result<ByteArray, FuelError> ->
            val (data, error) = result
            assertThat("Expected error, actual data $data", error, notNullValue())
        }

        running.join()
    }

    @Test
    fun responseResponseResultHandler() {
        val bytes = randomBytes()
        val running = getBytes(bytes).response { request, response, result  ->
            val (data, error) = result
            assertThat("Expected data, actual error $error", data, notNullValue())
            assertThat(data, equalTo(bytes))

            assertThat("Expected request to be not null", request, notNullValue())
            assertThat("Expected response to be not null", response, notNullValue())
        }

        running.join()

    }

    @Test
    fun responseResponseResultHandlerFailure() {
        val running = mocked404().response { request, response, result ->
            val (data, error) = result
            assertThat("Expected error, actual data $data", error, notNullValue())
            assertThat(error!!.exception as? HttpException, isA(HttpException::class.java))

            assertThat("Expected request to be not null", request, notNullValue())
            assertThat("Expected response to be not null", response, notNullValue())
        }

        running.join()
    }
}
