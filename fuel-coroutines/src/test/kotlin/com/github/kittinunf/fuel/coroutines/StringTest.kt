package com.github.kittinunf.fuel.coroutines

import awaitString
import awaitStringResponse
import awaitStringResponseResult
import awaitStringResult
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.test.MockHttpTestCase
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Assert.fail
import org.junit.Test
import java.net.HttpURLConnection

class StringTest : MockHttpTestCase() {

    private fun mocked404(method: Method = Method.GET, path: String = "invalid/url"): Request {
        mock.chain(
            request = mock.request().withPath("/$path"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )
        return Fuel.request(method, mock.path(path))
    }

    @Test
    fun awaitString() = runBlocking {
        try {
            val data = reflectedRequest(Method.GET, "ip").awaitString()
            assertThat("Expected data to be not null", data, notNullValue())
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
            assertThat("Expected request to be not null", request, notNullValue())
            assertThat("Expected response to be not null", response, notNullValue())
            assertThat("Expected data to be not null", data, notNullValue())
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
        assertThat("Expected data, actual error $error", data, notNullValue())
    }

    @Test
    fun awaitStringResultFailure() = runBlocking {
        val (data, error) = mocked404().awaitStringResult()
        assertThat("Expected error, actual data $data", error, notNullValue())
    }

    @Test
    fun awaitStringResponseResult() = runBlocking {
        val (request, response, result) = reflectedRequest(Method.GET, "ip").awaitStringResponseResult()
        val (data, error) = result
        assertThat("Expected data, actual error $error", data, notNullValue())
        assertThat("Expected request to be not null", request, notNullValue())
        assertThat("Expected response to be not null", response, notNullValue())
    }

    @Test
    fun awaitStringResponseResultFailure() = runBlocking {
        val (data, error) = mocked404().awaitStringResponseResult()
        assertThat("Expected error, actual data $data", error, notNullValue())
    }
}
