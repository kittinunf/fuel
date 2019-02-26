package com.github.kittinunf.fuel.coroutines

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.isSuccessful
import com.github.kittinunf.fuel.test.MockHttpTestCase
import com.github.kittinunf.result.Result
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.isA
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Assert.fail
import org.junit.Test
import java.net.ConnectException
import java.net.HttpURLConnection

class StringTest : MockHttpTestCase() {

    private fun mocked404(method: Method = Method.GET, path: String = "invalid/url"): Request {
        mock.chain(
            request = mock.request().withPath("/$path"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND).withHeader("foo", "bar").withBody("error:unauthorized")
        )
        return Fuel.request(method, mock.path(path))
    }

    @Test
    fun awaitString() = runBlocking {
        try {
            val data = reflectedRequest(Method.GET, "ip").awaitString()
            assertThat(data, notNullValue())
        } catch (exception: Exception) {
            fail("Expected pass, actual error $exception")
        }
    }

    @Test(expected = FuelError::class)
    fun awaitStringThrows() = runBlocking {
        val data = mocked404().awaitString()
        fail("Expected error, actual data $data")
    }

    @Test
    fun awaitStringResponse() = runBlocking {
        try {
            val (request, response, data) = reflectedRequest(Method.GET, "ip").awaitStringResponse()
            assertThat(request, notNullValue())
            assertThat(response, notNullValue())
            assertThat(data, notNullValue())
        } catch (exception: Exception) {
            fail("Expected pass, actual error $exception")
        }
    }

    @Test(expected = FuelError::class)
    fun awaitStringResponseThrows() = runBlocking {
        val (_, _, data) = mocked404().awaitStringResponse()
        fail("Expected error, actual data $data")
    }

    @Test
    fun awaitStringResult() = runBlocking {
        val (data, error) = reflectedRequest(Method.GET, "ip").awaitStringResult()
        assertThat(data, notNullValue())
    }

    @Test
    fun awaitStringResultFailure() = runBlocking {
        val (data, error) = mocked404().awaitStringResult()
        assertThat(error, notNullValue())
    }

    @Test
    fun awaitStringResponseResult() = runBlocking {
        val (request, response, result) = reflectedRequest(Method.GET, "ip").awaitStringResponseResult()
        val (data, error) = result
        assertThat(data, notNullValue())
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
    }

    @Test
    fun awaitStringResponseResultFailure() = runBlocking {
        val (data, error) = mocked404().awaitStringResponseResult()

        assertThat(data, notNullValue())
        assertThat(error, notNullValue())
        assertThat(error.statusCode, equalTo(404))
        assertThat(error.isSuccessful, equalTo(false))
        assertThat(error.headers["foo"], equalTo(listOf("bar") as Collection<String>))
    }

    @Test
    fun captureConnectException() = runBlocking {
        val (_, res, result) = Fuel.get("http://127.0.0.1:80")
                .awaitStringResponseResult()

        assertThat("Expected a response, actual null", res, notNullValue())

        val (data, error) = result
        assertThat("Expected error, actual data $data", error, notNullValue())

        when (result) {
            is Result.Success -> fail("should catch connect exception")
            is Result.Failure -> {
                assertThat(
                        result.error.exception as? ConnectException,
                        isA(ConnectException::class.java)
                )
            }
        }
    }
}
