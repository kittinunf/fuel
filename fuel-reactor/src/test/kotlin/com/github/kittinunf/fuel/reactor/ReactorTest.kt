package com.github.kittinunf.fuel.reactor

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.test.MockHttpTestCase
import com.github.kittinunf.result.Result
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import reactor.core.publisher.Mono
import reactor.core.publisher.onErrorResume
import reactor.test.test
import java.util.UUID

class ReactorTest : MockHttpTestCase() {

    @Test
    fun monoBytes() {
        mock.chain(
            request = mock.request().withPath("/bytes"),
            response = mock.response().withBody(ByteArray(10))
        )

        Fuel.get(mock.path("bytes")).monoBytes()
            .test()
            .assertNext { assertEquals(10, it.size) }
            .verifyComplete()
    }

    @Test
    fun monoString() {
        mock.chain(
            request = mock.request().withPath("/ip"),
            response = mock.response().withBody("127.0.0.1")
        )

        Fuel.get(mock.path("ip")).monoString()
            .test()
            .assertNext { assertEquals("127.0.0.1", it) }
            .verifyComplete()
    }

    @Test
    fun monoObject() {
        mock.chain(
            request = mock.request().withPath("/ip"),
            response = mock.response()
                .withBody(jacksonObjectMapper().writeValueAsString(Ip("127.0.0.1")))
        )

        Fuel.get(mock.path("ip")).monoObject(IpDeserializerSuccess)
            .map(Ip::origin)
            .test()
            .assertNext { assertEquals("127.0.0.1", it) }
            .verifyComplete()
    }

    @Test
    fun monoObjectWithInvalidFormat() {
        mock.chain(
            request = mock.request().withPath("/ip"),
            response = mock.response()
                .withBody(jacksonObjectMapper().writeValueAsString(Ip("127.0.0.1")))
        )

        Fuel.get(mock.path("ip")).monoObject(IpLongDeserializer)
            .map(IpLong::origin)
            .test()
            .expectErrorMatches { (it as FuelError).exception is InvalidFormatException }
            .verify()
    }

    @Test
    fun monoObjectWithMissingProperty() {
        mock.chain(
            request = mock.request().withPath("/ip"),
            response = mock.response()
                .withBody(jacksonObjectMapper().writeValueAsString(Ip("127.0.0.1")))
        )

        val errorMessage = Fuel.get(mock.path("ip")).monoObject(IpAddressDeserializer)
            .map(IpAddress::address)
            .onErrorResume(FuelError::class) {
                assertTrue(it.exception is MissingKotlinParameterException)
                Mono.just(it.message.orEmpty())
            }
            .block()!!

        assertTrue(errorMessage.contains("value failed for JSON property address due to missing"))
    }

    @Test
    fun monoResponse() {
        mock.chain(
            request = mock.request().withPath("/status"),
            response = mock.response().withStatusCode(404)
        )

        Fuel.get(mock.path("status")).monoResponse()
            .map(Response::statusCode)
            .test()
            .assertNext { assertEquals(it, 404) }
            .verifyComplete()
    }

    @Test
    fun monoResultBytes() {
        mock.chain(
            request = mock.request().withPath("/bytes"),
            response = mock.response().withBody(ByteArray(20))
        )

        Fuel.get(mock.path("bytes")).monoResultBytes()
            .map(Result<ByteArray, FuelError>::get)
            .test()
            .assertNext { assertEquals(20, it.size) }
            .verifyComplete()
    }

    @Test
    fun monoResultString() {
        val randomUuid = UUID.randomUUID().toString()

        mock.chain(
            request = mock.request().withPath("/uuid"),
            response = mock.response().withBody(randomUuid)
        )

        Fuel.get(mock.path("uuid")).monoResultString()
            .map(Result<String, FuelError>::get)
            .test()
            .assertNext { assertEquals(randomUuid, it) }
            .verifyComplete()
    }

    @Test
    fun monoResultObject() {
        mock.chain(
            request = mock.request().withPath("/ip"),
            response = mock.response()
                .withBody(jacksonObjectMapper().writeValueAsString(Ip("192.168.0.1")))
        )

        Fuel.get(mock.path("ip")).monoResultObject(IpDeserializerSuccess)
            .map(Result<Ip, FuelError>::get)
            .map(Ip::origin)
            .test()
            .assertNext { assertEquals("192.168.0.1", it) }
            .verifyComplete()
    }

    @Test
    fun monoResultObjectWithInvalidFormat() {
        mock.chain(
            request = mock.request().withPath("/ip"),
            response = mock.response()
                .withBody(jacksonObjectMapper().writeValueAsString(Ip("192.168.0.1")))
        )

        Fuel.get(mock.path("ip")).monoResultObject(IpLongDeserializer)
            .map(Result<IpLong, FuelError>::component2)
            .test()
            .assertNext { assertTrue(it?.exception is InvalidFormatException) }
            .verifyComplete()
    }

    @Test
    fun monoResultObjectWithMissingProperty() {
        mock.chain(
            request = mock.request().withPath("/ip"),
            response = mock.response()
                .withBody(jacksonObjectMapper().writeValueAsString(Ip("192.168.0.1")))
        )

        Fuel.get(mock.path("ip")).monoResultObject(IpAddressDeserializer)
            .map(Result<IpAddress, FuelError>::component2)
            .test()
            .assertNext { assertTrue(it?.exception is MissingKotlinParameterException) }
            .verifyComplete()
    }

    @Test
    fun monoCancellation() {
        mock.chain(
                request = mock.request().withPath("/bytes"),
                response = mock.response().withBody(ByteArray(10))
        )

        val running = Fuel.get(mock.path("bytes"))
            .monoBytes()
            .toProcessor()

        running.cancel()
        assertThat(running.isCancelled, equalTo(true))
    }

    @Test
    fun monoUnit() {
        mock.chain(
            request = mock.request().withPath("/ip"),
            response = mock.response().withBody("127.0.0.1")
        )

        Fuel.get(mock.path("ip")).monoUnit()
            .test()
            .assertNext { assertThat(it, equalTo(Unit)) }
            .verifyComplete()
    }

    private data class IpLong(val origin: Long)

    private object IpLongDeserializer : ResponseDeserializable<IpLong> {
        override fun deserialize(content: String) = jacksonObjectMapper().readValue<IpLong>(content)
    }

    private data class IpAddress(val address: String)

    private object IpAddressDeserializer : ResponseDeserializable<IpAddress> {
        override fun deserialize(content: String) = jacksonObjectMapper().readValue<IpAddress>(content)
    }

    private data class Ip(val origin: String)

    private object IpDeserializerSuccess : ResponseDeserializable<Ip> {
        override fun deserialize(content: String) = jacksonObjectMapper().readValue<Ip>(content)
    }
}
