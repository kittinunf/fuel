package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Handler
import com.github.kittinunf.fuel.core.HttpException
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.core.ResponseHandler
import com.github.kittinunf.fuel.core.response
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

    private fun randomUUID() = UUID.randomUUID()
    private fun getUUID(uuid: UUID, path: String = "uuid"): Request {
        mock.chain(
            request = mock.request().withPath("/$path"),
            response = mock.response().withBody(uuid.toString())
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
        val uuid = randomUUID()
        val (request, response, result) = getUUID(uuid).response(UUIDResponseDeserializer)
        val (data, error) = result
        assertThat("Expected data, actual error $error", data, notNullValue())

        assertThat(data!!.uuid, equalTo(uuid.toString()))
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
        val uuid = randomUUID()
        val running = getUUID(uuid).response(UUIDResponseDeserializer, object : Handler<UUIDResponse> {
            override fun success(value: UUIDResponse) {
                assertThat(value.uuid, equalTo(uuid.toString()))
            }

            override fun failure(error: FuelError) {
                fail("Expected data, actual error $error")
            }
        })

        running.join()
    }

    @Test
    fun responseHandlerFailure() {
        val running = mocked404().response(UUIDResponseDeserializer, object : Handler<UUIDResponse> {
            override fun success(value: UUIDResponse) {
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
        val uuid = randomUUID()
        val running = getUUID(uuid).response(UUIDResponseDeserializer, object : ResponseHandler<UUIDResponse> {
            override fun success(request: Request, response: Response, value: UUIDResponse) {
                assertThat("Expected data to be not null", value, notNullValue())
                assertThat(value.uuid, equalTo(uuid.toString()))

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
        val running = mocked404().response(UUIDResponseDeserializer, object : ResponseHandler<UUIDResponse> {
            override fun success(request: Request, response: Response, value: UUIDResponse) {
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
        val uuid = randomUUID()
        val running = getUUID(uuid).response(UUIDResponseDeserializer) { result: Result<UUIDResponse, FuelError> ->
            val (data, error) = result
            assertThat("Expected data, actual error $error", data, notNullValue())
            assertThat(data!!.uuid, equalTo(uuid.toString()))
        }

        running.join()
    }

    @Test
    fun responseResultHandlerFailure() {
        val running = mocked404().response(UUIDResponseDeserializer) { result: Result<UUIDResponse, FuelError> ->
            val (data, error) = result
            assertThat("Expected error, actual data $data", error, notNullValue())
        }

        running.join()
    }

    @Test
    fun responseResponseResultHandler() {
        val uuid = randomUUID()
        val running = getUUID(uuid).response(UUIDResponseDeserializer) { request, response, result ->
            val (data, error) = result
            assertThat("Expected data, actual error $error", data, notNullValue())
            assertThat(data!!.uuid, equalTo(uuid.toString()))

            assertThat("Expected request to be not null", request, notNullValue())
            assertThat("Expected response to be not null", response, notNullValue())
        }

        running.join()
    }

    @Test
    fun responseResponseResultHandlerFailure() {
        val running = mocked404().response(UUIDResponseDeserializer) { request, response, result ->
            val (data, error) = result
            assertThat("Expected error, actual data $data", error, notNullValue())
            assertThat(error!!.exception as? HttpException, isA(HttpException::class.java))

            assertThat("Expected request to be not null", request, notNullValue())
            assertThat("Expected response to be not null", response, notNullValue())
        }

        running.join()
    }
}
