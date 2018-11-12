package com.github.kittinunf.fuel.coroutines

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.test.MockHttpTestCase
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.isA
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Assert.fail
import org.junit.Test
import java.net.HttpURLConnection
import java.util.UUID

private data class UUIDResponse(val uuid: String)

private object UUIDResponseDeserializer : ResponseDeserializable<UUIDResponse> {

    class NoValidFormat(m: String = "Not a UUID") : Exception(m)

    override fun deserialize(content: String): UUIDResponse {
        if (content.contains("=") || !content.contains("-")) {
            throw FuelError.wrap(NoValidFormat())
        }
        return UUIDResponse(content)
    }
}

class ObjectTest : MockHttpTestCase() {

    private fun mocked404(method: Method = Method.GET, path: String = "invalid/url"): Request {
        mock.chain(
            request = mock.request().withPath("/$path"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )
        return Fuel.request(method, mock.path(path))
    }

    private fun randomUuid(path: String = "uuid"): Request {
        mock.chain(
            request = mock.request().withPath("/$path"),
            response = mock.response().withBody(UUID.randomUUID().toString())
        )
        return Fuel.request(Method.GET, mock.path(path))
    }

    @Test
    fun awaitObject() = runBlocking {
        try {
            val data = randomUuid().awaitObject(UUIDResponseDeserializer)
            assertThat("Expected data to be not null", data, notNullValue())
        } catch (exception: Exception) {
            fail("Expected pass, actual error $exception")
        }
    }

    @Test(expected = FuelError::class)
    fun awaitObjectThrows() = runBlocking {
        val data = mocked404().awaitObject(UUIDResponseDeserializer)
        fail("Expected error, actual data $data")
    }

    @Test
    fun awaitObjectResponse() = runBlocking {
        try {
            val (request, response, data) = randomUuid().awaitObjectResponse(UUIDResponseDeserializer)
            assertThat("Expected request to be not null", request, notNullValue())
            assertThat("Expected response to be not null", response, notNullValue())
            assertThat("Expected data to be not null", data, notNullValue())
        } catch (exception: Exception) {
            fail("Expected pass, actual error $exception")
        }
    }

    @Test(expected = FuelError::class)
    fun awaitObjectResponseThrows() = runBlocking {
        val (_, _, data) = mocked404().awaitObjectResponse(UUIDResponseDeserializer)
        fail("Expected error, actual data $data")
    }

    @Test
    fun awaitObjectResult() = runBlocking {
        val (data, error) = randomUuid().awaitObjectResult(UUIDResponseDeserializer)
        assertThat("Expected data, actual error $error", data, notNullValue())
    }

    @Test
    fun awaitObjectResultFailure() = runBlocking {
        val (data, error) = mocked404().awaitObjectResult(UUIDResponseDeserializer)
        assertThat("Expected error, actual data $data", error, notNullValue())
    }

    @Test
    fun awaitObjectResponseResult() = runBlocking {
        val (request, response, result) = randomUuid().awaitObjectResponseResult(UUIDResponseDeserializer)
        val (data, error) = result
        assertThat("Expected data, actual error $error", data, notNullValue())
        assertThat("Expected request to be not null", request, notNullValue())
        assertThat("Expected response to be not null", response, notNullValue())
    }

    @Test
    fun awaitObjectResponseResultFailure() = runBlocking {
        val (data, error) = mocked404().awaitObjectResponseResult(UUIDResponseDeserializer)
        assertThat("Expected error, actual data $data", error, notNullValue())
    }

    @Test
    fun captureDeserializationException() = runBlocking {

        val (request, response, result) = reflectedRequest(Method.GET, "reflected")
            .awaitObjectResponseResult(object : ResponseDeserializable<Unit> {
                override fun deserialize(content: String): Unit? {
                    throw IllegalStateException("some deserialization exception")
                }
            })

        val (data, error) = result
        assertThat("Expected error, actual $data", error, notNullValue())
        assertThat("Expected response to be not null", response, notNullValue())
        assertThat("Expected request to be not null", request, notNullValue())

        assertThat(error, isA(FuelError::class.java))
        assertThat((error as FuelError).exception as? IllegalStateException, isA(IllegalStateException::class.java))
    }
}
