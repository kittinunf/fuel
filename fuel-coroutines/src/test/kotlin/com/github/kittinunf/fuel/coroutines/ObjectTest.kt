package com.github.kittinunf.fuel.coroutines

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.core.isSuccessful
import com.github.kittinunf.fuel.test.MockHttpTestCase
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
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

    private fun mocked401(method: Method = Method.GET, path: String = "invalid/url"): Request {
        mock.chain(
            request = mock.request().withPath("/$path"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_UNAUTHORIZED).withHeader("foo", "bar").withBody("error:unauthorized")
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
            assertThat(data, notNullValue())
        } catch (exception: Exception) {
            fail("Expected pass, actual error $exception")
        }
    }

    @Test(expected = FuelError::class)
    fun awaitObjectThrows() = runBlocking {
        val data = mocked401().awaitObject(UUIDResponseDeserializer)
        fail("Expected error, actual data $data")
    }

    @Test
    fun awaitObjectResponse() = runBlocking {
        try {
            val (request, response, data) = randomUuid().awaitObjectResponse(UUIDResponseDeserializer)
            assertThat(request, notNullValue())
            assertThat(response, notNullValue())
            assertThat(data, notNullValue())
        } catch (exception: Exception) {
            fail("Expected pass, actual error $exception")
        }
    }

    @Test(expected = FuelError::class)
    fun awaitObjectResponseThrows() = runBlocking {
        val (_, _, data) = mocked401().awaitObjectResponse(UUIDResponseDeserializer)
        fail("Expected error, actual data $data")
    }

    @Test
    fun awaitObjectResult() = runBlocking {
        val (data, error) = randomUuid().awaitObjectResult(UUIDResponseDeserializer)
        assertThat(data, notNullValue())
    }

    @Test
    fun awaitObjectResultFailure() = runBlocking {
        val (data, error) = mocked401().awaitObjectResult(UUIDResponseDeserializer)
        assertThat(error, notNullValue())
    }

    @Test
    fun awaitObjectResponseResult() = runBlocking {
        val (request, response, result) = randomUuid().awaitObjectResponseResult(UUIDResponseDeserializer)
        val (data, error) = result
        assertThat(data, notNullValue())
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
    }

    @Test
    fun awaitObjectResponseResultFailure() = runBlocking {
        val (data, response, result) = mocked401().awaitObjectResponseResult(UUIDResponseDeserializer)

        assertThat(data, notNullValue())
        assertThat(response, notNullValue())
        assertThat(response.statusCode, equalTo(HttpURLConnection.HTTP_UNAUTHORIZED))
        assertThat(response.isSuccessful, equalTo(false))
        assertThat(response.headers["foo"], equalTo(listOf("bar") as Collection<String>))

        val (_, error) = result
        assertThat(error!!.response, equalTo(response))
        assertThat(error.response.statusCode, equalTo(response.statusCode))
        assertThat(error.response.body(), equalTo(response.body()))
    }

    @Test
    fun captureDeserializationException() = runBlocking {

        val (request, response, result) = reflectedRequest(Method.GET, "reflected")
            .awaitObjectResponseResult(object : ResponseDeserializable<Unit> {
                override fun deserialize(content: String): Unit? {
                    throw IllegalStateException("some deserialization exception")
                }
            })

        val (_, error) = result
        assertThat(error, notNullValue())
        assertThat(response, notNullValue())
        assertThat(request, notNullValue())

        assertThat(response.statusCode, equalTo(200))
        assertThat(response.responseMessage, equalTo("OK"))
        assertThat(error, isA(FuelError::class.java))
        assertThat((error as FuelError).exception as? IllegalStateException, isA(IllegalStateException::class.java))
    }
}
