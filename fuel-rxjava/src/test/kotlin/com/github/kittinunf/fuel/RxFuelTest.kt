package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.core.response
import com.github.kittinunf.fuel.rx.rx
import com.github.kittinunf.fuel.rx.rx_bytes
import com.github.kittinunf.fuel.rx.rx_object
import com.github.kittinunf.fuel.rx.rx_response
import com.github.kittinunf.fuel.rx.rx_responseObject
import com.github.kittinunf.fuel.rx.rx_responseString
import com.github.kittinunf.fuel.rx.rx_string
import com.github.kittinunf.result.Result
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.core.Is.isA
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.io.InputStream
import java.net.HttpURLConnection
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RxFuelTest {

    init {
        Fuel.testMode {
            timeout = 15000
        }
    }

    private lateinit var mock: MockHelper

    @Before
    fun setup() {
        this.mock = MockHelper()
        this.mock.setup()
    }

    @After
    fun tearDown() {
        this.mock.tearDown()
    }


    @Test
    fun rxTestResponse() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        val (response, data) = Fuel.get(mock.path("user-agent"))
                .rx_response()
                .test()
                .apply { awaitTerminalEvent() }
                .assertNoErrors()
                .assertValueCount(1)
                .assertComplete()
                .values()[0]

        assertThat(response, notNullValue())
        assertThat(data, notNullValue())
    }

    @Test
    fun rxTestResponseString() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        val (response, data) = Fuel.get(mock.path("user-agent")).rx_responseString()
                .test()
                .apply { awaitTerminalEvent() }
                .assertNoErrors()
                .assertValueCount(1)
                .assertComplete()
                .values()[0]

        assertThat(response, notNullValue())
        assertThat(data, notNullValue())
    }

    @Test
    fun rxBytes() {
        mock.chain(
            request = mock.request().withPath("/bytes"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_OK).withBody(ByteArray(555) { 0 })
        )

        val data = Fuel.get(mock.path("bytes")).rx_bytes()
                .test()
                .apply { awaitTerminalEvent() }
                .assertNoErrors()
                .assertValueCount(1)
                .assertComplete()
                .values()[0]

        assertThat(data, notNullValue())
        assertThat(data as Result.Success, isA(Result.Success::class.java))
        val (value, error) = data
        assertThat(value, notNullValue())
        assertThat(error, nullValue())
    }

    @Test
    fun rxTestString() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        val data = Fuel.get(mock.path("user-agent")).rx_string()
                .test()
                .apply { awaitTerminalEvent() }
                .assertNoErrors()
                .assertValueCount(1)
                .assertComplete()
                .values()[0]

        assertThat(data, notNullValue())
        assertThat(data as Result.Success, isA(Result.Success::class.java))
        val (value, error) = data
        assertThat(value, notNullValue())
        assertThat(error, nullValue())
    }

    @Test
    fun rxTestStringError() {
        mock.chain(
            request = mock.request().withPath("/error"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )
        val data = Fuel.get(mock.path("error")).rx_string()
                .test()
                .apply { awaitTerminalEvent() }
                .assertNoErrors()
                .assertValueCount(1)
                .assertComplete()
                .values()[0]

        assertThat(data as Result.Failure, isA(Result.Failure::class.java))
        val (value, error) = data
        assertThat(value, nullValue())
        assertThat(error, notNullValue())
        assertThat(error?.exception?.message, containsString("404 Not Found"))
    }

    //Model
    data class HttpBinUserAgentModel(var userAgent: String = "")

    //Deserializer
    class HttpBinUserAgentModelDeserializer : ResponseDeserializable<HttpBinUserAgentModel> {
        override fun deserialize(content: String): HttpBinUserAgentModel? = HttpBinUserAgentModel(content)
    }

    class HttpBinMalformedDeserializer : ResponseDeserializable<HttpBinUserAgentModel> {
        override fun deserialize(inputStream: InputStream): HttpBinUserAgentModel? = throw IllegalStateException("Malformed data")
    }

    @Test
    fun rxTestResponseObject() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        val (response, result) = Fuel.get(mock.path("user-agent"))
                .rx_responseObject(HttpBinUserAgentModelDeserializer())
                .test()
                .apply { awaitTerminalEvent() }
                .assertNoErrors()
                .assertValueCount(1)
                .assertComplete()
                .values()[0]

        assertThat(response, notNullValue())
        assertThat(result, notNullValue())
        assertThat(result as Result.Success, isA(Result.Success::class.java))
        val (value, error) = result
        assertThat(value, notNullValue())
        assertThat(error, nullValue())
    }

    @Test
    fun rxTestResponseObjectError() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        val (response, result) = Fuel.get(mock.path("user-agent"))
                .rx_responseObject(HttpBinUserAgentModelDeserializer())
                .test()
                .apply { awaitTerminalEvent() }
                .assertNoErrors()
                .assertValueCount(1)
                .assertComplete()
                .values()[0]

        assertThat(response, notNullValue())
        assertThat(result as Result.Failure, isA(Result.Failure::class.java))
        val (value, error) = result
        assertThat(value, nullValue())
        assertThat(error, notNullValue())
    }

    @Test
    fun rxTestResponseObjectMalformed() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        val (response, result) = Fuel.get(mock.path("user-agent"))
                .rx_responseObject(HttpBinMalformedDeserializer())
                .test()
                .apply { awaitTerminalEvent() }
                .assertNoErrors()
                .assertValueCount(1)
                .assertComplete()
                .values()[0]

        assertThat(response, notNullValue())
        assertThat(result as Result.Failure, isA(Result.Failure::class.java))
        assertThat(result.error.exception as IllegalStateException, isA(IllegalStateException::class.java))
        assertThat(result.error.exception.message, isEqualTo("Malformed data"))
    }

    @Test
    fun rxTestWrapper() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        val (request, response, result) =
                Fuel.get(mock.path("user-agent"))
                        .rx { response(HttpBinUserAgentModelDeserializer()) }
                        .test()
                        .apply { awaitTerminalEvent() }
                        .assertNoErrors()
                        .assertValueCount(1)
                        .assertComplete()
                        .values()[0]

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(result as Result.Success, isA(Result.Success::class.java))
        val (value, error) = result
        assertThat(value, notNullValue())
        assertThat(error, nullValue())
    }

    @Test
    fun rxTestObject() {
        mock.chain(
                request = mock.request().withPath("/user-agent"),
                response = mock.reflect()
        )

        val data = Fuel.get(mock.path("user-agent"))
                .rx_object(HttpBinUserAgentModelDeserializer())
                .test()
                .apply { awaitTerminalEvent() }
                .assertNoErrors()
                .assertValueCount(1)
                .assertComplete()
                .values()[0]

        assertThat(data, notNullValue())
        assertThat(data as Result.Success, isA(Result.Success::class.java))
        val (value, error) = data
        assertThat(value, notNullValue())
        assertThat(error, nullValue())
    }
}
