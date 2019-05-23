package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.Deserializable
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Method.GET
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.rx.rxBytes
import com.github.kittinunf.fuel.rx.rxBytesPair
import com.github.kittinunf.fuel.rx.rxObject
import com.github.kittinunf.fuel.rx.rxResponse
import com.github.kittinunf.fuel.rx.rxResponseObject
import com.github.kittinunf.fuel.rx.rxResponseObjectPair
import com.github.kittinunf.fuel.rx.rxResponseObjectTriple
import com.github.kittinunf.fuel.rx.rxResponsePair
import com.github.kittinunf.fuel.rx.rxResponseString
import com.github.kittinunf.fuel.rx.rxResponseStringPair
import com.github.kittinunf.fuel.rx.rxResponseStringTriple
import com.github.kittinunf.fuel.rx.rxResponseTriple
import com.github.kittinunf.fuel.rx.rxString
import com.github.kittinunf.fuel.test.MockHttpTestCase
import com.github.kittinunf.result.Result
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.core.Is.isA
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.InputStream
import java.net.HttpURLConnection
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RxFuelTest : MockHttpTestCase() {

    @Test
    fun rxResponse() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        val data = Fuel.get(mock.path("user-agent"))
            .rxResponse()
            .test()
            .apply { awaitTerminalEvent() }
            .assertNoErrors()
            .assertValueCount(1)
            .assertComplete()
            .values()[0]

        assertThat(data, notNullValue())
    }

    @Test
    fun rxResponsePair() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        val (response, data) = Fuel.get(mock.path("user-agent"))
            .rxResponsePair()
            .test()
            .apply { awaitTerminalEvent() }
            .assertNoErrors()
            .assertValueCount(1)
            .assertComplete()
            .values()[0]

        assertThat(response, notNullValue())
        assertThat(response.statusCode, equalTo(HttpURLConnection.HTTP_OK))
        assertThat(data, notNullValue())
    }

    @Test
    fun rxResponseTriple() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        val (request, response, data) = Fuel.get(mock.path("user-agent"))
            .rxResponseTriple()
            .test()
            .apply { awaitTerminalEvent() }
            .assertNoErrors()
            .assertValueCount(1)
            .assertComplete()
            .values()[0]

        assertThat(request, notNullValue())
        assertThat(request.method, equalTo(GET))
        assertThat(response, notNullValue())
        assertThat(response.statusCode, equalTo(HttpURLConnection.HTTP_OK))
        assertThat(data, notNullValue())
    }

    @Test
    fun rxResponseString() {
        mock.chain(
                request = mock.request().withPath("/user-agent"),
                response = mock.reflect()
        )

        val data = Fuel.get(mock.path("user-agent"))
                .rxResponseString()
                .test()
                .apply { awaitTerminalEvent() }
                .assertNoErrors()
                .assertValueCount(1)
                .assertComplete()
                .values()[0]

        assertThat(data, notNullValue())
    }

    @Test
    fun rxResponseStringPair() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        val (response, data) = Fuel.get(mock.path("user-agent"))
            .rxResponseStringPair()
            .test()
            .apply { awaitTerminalEvent() }
            .assertNoErrors()
            .assertValueCount(1)
            .assertComplete()
            .values()[0]

        assertThat(response, notNullValue())
        assertThat(response.statusCode, equalTo(HttpURLConnection.HTTP_OK))
        assertThat(data, notNullValue())
    }

    @Test
    fun rxResponseStringTriple() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        val (request, response, data) = Fuel.get(mock.path("user-agent"))
            .rxResponseStringTriple()
            .test()
            .apply { awaitTerminalEvent() }
            .assertNoErrors()
            .assertValueCount(1)
            .assertComplete()
            .values()[0]

        assertThat(response, notNullValue())
        assertThat(request, notNullValue())
        assertThat(response.statusCode, equalTo(HttpURLConnection.HTTP_OK))
        assertThat(data, notNullValue())
    }

    data class Foo(val string: String)

    @Test
    fun rxResponseObject() {
        mock.chain(
                request = mock.request().withPath("/user-agent"),
                response = mock.reflect()
        )

        val data = Fuel.get(mock.path("user-agent"))
                .rxResponseObject(object : Deserializable<Foo> {
                    override fun deserialize(response: Response): Foo = Foo("user-agent")
                })
                .test()
                .apply { awaitTerminalEvent() }
                .assertNoErrors()
                .assertValueCount(1)
                .assertComplete()
                .values()[0]

        assertThat(data, notNullValue())
        assertThat(data.string, equalTo("user-agent"))
    }

    @Test
    fun rxResponseObjectTriple() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        val (request, response, data) = Fuel.get(mock.path("user-agent"))
            .rxResponseObjectTriple(object : Deserializable<Foo> {
                override fun deserialize(response: Response): Foo = Foo("xxx")
            })
            .test()
            .apply { awaitTerminalEvent() }
            .assertNoErrors()
            .assertValueCount(1)
            .assertComplete()
            .values()[0]

        assertThat(response, notNullValue())
        assertThat(request, notNullValue())
        assertThat(response.statusCode, equalTo(HttpURLConnection.HTTP_OK))
        assertThat(data, notNullValue())
        assertThat(data.string, equalTo("xxx"))
    }

    @Test
    fun rxResponseObjectTripleWithError() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        val err = Fuel.get(mock.path("user-agent"))
                .rxResponseObjectTriple(object : Deserializable<Foo> {
                    override fun deserialize(response: Response): Foo = throw error("error")
                })
                .test()
                .apply { awaitTerminalEvent() }
                .assertError(FuelError::class.java)
                .assertNoValues()
                .errors()[0]

        assertThat(err, notNullValue())
    }

    @Test
    fun rxBytes() {
        mock.chain(
            request = mock.request().withPath("/bytes"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_OK).withBody(ByteArray(555) { 0 })
        )

        val data = Fuel.get(mock.path("bytes"))
            .rxBytes()
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
    fun rxBytesPair() {
        mock.chain(
            request = mock.request().withPath("/bytes"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_OK).withBody(ByteArray(555) { 0 })
        )

        val data = Fuel.get(mock.path("bytes"))
            .rxBytesPair()
            .test()
            .apply { awaitTerminalEvent() }
            .assertNoErrors()
            .assertValueCount(1)
            .assertComplete()
            .values()[0]

        assertThat(data, notNullValue())
        assertThat(data.first.statusCode, equalTo(HttpURLConnection.HTTP_OK))
        assertThat(data.second as Result.Success, isA(Result.Success::class.java))
        val (value, error) = data
        assertThat(value, notNullValue())
        assertThat(error, nullValue())
    }

    @Test
    fun rxString() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        val data = Fuel.get(mock.path("user-agent"))
            .rxString()
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
    fun rxStringWithError() {
        mock.chain(
            request = mock.request().withPath("/error"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )
        val data = Fuel.get(mock.path("error"))
            .rxString()
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

    // Model
    data class HttpBinUserAgentModel(var userAgent: String = "")

    // Deserializer
    class HttpBinUserAgentModelDeserializer : ResponseDeserializable<HttpBinUserAgentModel> {
        override fun deserialize(content: String): HttpBinUserAgentModel? = HttpBinUserAgentModel(content)
    }

    class HttpBinMalformedDeserializer : ResponseDeserializable<HttpBinUserAgentModel> {
        override fun deserialize(inputStream: InputStream): HttpBinUserAgentModel? = throw IllegalStateException("Malformed data")
    }

    @Test
    fun rxResponseObjectPair() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        val (response, result) = Fuel.get(mock.path("user-agent"))
            .rxResponseObjectPair(HttpBinUserAgentModelDeserializer())
            .test()
            .apply { awaitTerminalEvent() }
            .assertNoErrors()
            .assertValueCount(1)
            .assertComplete()
            .values()
            .first()

        assertThat(response, notNullValue())
        assertThat(result, notNullValue())
        assertThat(result, isA(HttpBinUserAgentModel::class.java))
    }

    @Test
    fun rxResponseObjectPairWithError() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        val single = Fuel.get(mock.path("user-agent"))
            .rxResponseObjectPair(HttpBinUserAgentModelDeserializer())
            .test()
            .apply { awaitTerminalEvent() }
            .assertError(FuelError::class.java)
            .assertNoValues()

        val error = single.errors().firstOrNull()
        val (response, value) = single.values().firstOrNull() ?: Pair(null, null)

        assertThat("Expected error, actual response $response", response, nullValue())
        assertThat("Expected error, actual value $value", value, nullValue())
        assertThat(error, notNullValue())
    }

    @Test
    fun rxResponseObjectPairWithMalformed() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        val single = Fuel.get(mock.path("user-agent"))
            .rxResponseObjectPair(HttpBinMalformedDeserializer())
            .test()
            .apply { awaitTerminalEvent() }
            .assertError(FuelError::class.java)
            .assertNoValues()

        val error = single.errors().firstOrNull()
        val (response, value) = single.values().firstOrNull() ?: Pair(null, null)

        assertThat("Expected error, actual response $response", response, nullValue())
        assertThat("Expected error, actual value $value", value, nullValue())

        val fuelError = error as? FuelError
        assertThat(fuelError, isA(FuelError::class.java))
        assertThat(fuelError!!.exception as IllegalStateException, isA(IllegalStateException::class.java))
        assertThat(fuelError.exception.message, isEqualTo("Malformed data"))
    }

    @Test
    fun rxObject() {
        mock.chain(
                request = mock.request().withPath("/user-agent"),
                response = mock.reflect()
        )

        val data = Fuel.get(mock.path("user-agent"))
            .rxObject(HttpBinUserAgentModelDeserializer())
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
