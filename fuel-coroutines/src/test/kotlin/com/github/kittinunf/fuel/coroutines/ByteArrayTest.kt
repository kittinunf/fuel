package com.github.kittinunf.fuel.coroutines

import awaitByteArray
import awaitByteArrayResponse
import awaitByteArrayResponseResult
import awaitByteArrayResult
import awaitObjectResponseResult
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.test.MockHttpTestCase
import com.github.kittinunf.result.Result
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.isA
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThat
import org.junit.Assert.fail
import org.junit.Test
import java.net.HttpURLConnection

class ByteArrayTest : MockHttpTestCase() {

    private fun mocked404(method: Method = Method.GET, path: String = "invalid/url"): Request {
        mock.chain(
            request = mock.request().withPath("/$path"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )
        return Fuel.request(method, mock.path(path))
    }

    @Test
    fun awaitByteArray() = runBlocking {
        try {
            val data = reflectedRequest(Method.GET, "ip").awaitByteArray()
            assertThat("Expected data to be not null", data, notNullValue())
        } catch (exception: Exception) {
            fail("Expected pass, actual error $exception")
        }
    }

    @Test(expected = FuelError::class)
    fun awaitByteArrayThrows() = runBlocking {
        val data = mocked404().awaitByteArray()
        fail("Expected error, actual data $data")
    }

    @Test
    fun awaitByteArrayResponse() = runBlocking {
        try {
            val (request, response, data) = reflectedRequest(Method.GET, "ip").awaitByteArrayResponse()
            assertThat("Expected request to be not null", request, notNullValue())
            assertThat("Expected response to be not null", response, notNullValue())
            assertThat("Expected data to be not null", data, notNullValue())
        } catch (exception: Exception) {
            fail("Expected pass, actual error $exception")
        }
    }

    @Test(expected = FuelError::class)
    fun awaitByteArrayResponseThrows() = runBlocking {
        val (_, _, data) = mocked404().awaitByteArrayResponse()
        fail("Expected error, actual data $data")
    }

    @Test
    fun awaitByteArrayResult() = runBlocking {
        val (data, error) = reflectedRequest(Method.GET, "ip").awaitByteArrayResult()
        assertThat("Expected data, actual error $error", data, notNullValue())
    }

    @Test
    fun awaitByteArrayResultFailure() = runBlocking {
        val (data, error) = mocked404().awaitByteArrayResult()
        assertThat("Expected error, actual data $data", error, notNullValue())
    }

    @Test
    fun awaitByteArrayResponseResult() = runBlocking {
        val (request, response, result) = reflectedRequest(Method.GET, "ip").awaitByteArrayResponseResult()
        val (data, error) = result
        assertThat("Expected data, actual error $error", data, notNullValue())
        assertThat("Expected request to be not null", request, notNullValue())
        assertThat("Expected response to be not null", response, notNullValue())
    }

    @Test
    fun awaitByteArrayResponseResultFailure() = runBlocking {
        val (data, error) = mocked404().awaitByteArrayResponseResult()
        assertThat("Expected error, actual data $data", error, notNullValue())
    }
}
