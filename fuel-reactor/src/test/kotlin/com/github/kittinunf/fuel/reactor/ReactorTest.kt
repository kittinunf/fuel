package com.github.kittinunf.fuel.reactor

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.result.Result
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import reactor.core.publisher.Mono
import reactor.core.publisher.onErrorResume
import reactor.test.test

class ReactorTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun enableFuelTestingMode() {
            Fuel.testMode { timeout = 5000 }
            FuelManager.instance.basePath = "https://httpbin.org"
        }
    }

    @Test
    fun testBytes() {
        Fuel.get("/ip").monoBytes()
            .test()
            .assertNext { assertTrue(it.size > 0) }
            .verifyComplete()
    }

    @Test
    fun testString() {
        Fuel.get("/uuid").monoString()
            .test()
            .assertNext { assertTrue(it.isNotEmpty()) }
            .verifyComplete()
    }

    @Test
    fun testObjectSuccess() {
        Fuel.get("/ip").monoObject(IpDeserializerSuccess)
            .map(Ip::origin)
            .test()
            .assertNext { assertTrue(it.isNotEmpty()) }
            .verifyComplete()
    }

    @Test
    fun testObjectFailureWrongType() {
        Fuel.get("/ip").monoObject(IpLongDeserializer)
            .map(IpLong::origin)
            .test()
            .expectErrorMatches { (it as FuelError).exception is InvalidFormatException }
            .verify()
    }

    @Test
    fun testObjectFailureMissingProperty() {
        val errorMessage = Fuel.get("/ip").monoObject(IpAddressDeserializer)
            .map(IpAddress::address)
            .onErrorResume(FuelError::class, {
                assertTrue(it.exception is MissingKotlinParameterException)
                Mono.just(it.message.orEmpty())
            })
            .block()!!

        assertTrue(errorMessage.contains("value failed for JSON property address due to missing"))
    }

    @Test
    fun testResponse() {
        Fuel.get("/status/404").monoResponse()
            .map(Response::statusCode)
            .test()
            .assertNext { assertEquals(it, 404) }
            .verifyComplete()
    }

    @Test
    fun testResultBytes() {
        Fuel.get("/bytes/10").monoResultBytes()
            .map(Result<ByteArray, FuelError>::get)
            .test()
            .assertNext { assertTrue(it.size == 10) }
            .verifyComplete()
    }

    @Test
    fun testResultString() {
        Fuel.get("/uuid").monoResultString()
            .map(Result<String, FuelError>::get)
            .test()
            .assertNext { assertTrue(it.isNotEmpty()) }
            .verifyComplete()
    }

    @Test
    fun testResultObjectSuccess() {
        Fuel.get("/ip").monoResultObject(IpDeserializerSuccess)
            .map(Result<Ip, FuelError>::get)
            .map(Ip::origin)
            .test()
            .assertNext { assertTrue(it.isNotEmpty()) }
            .verifyComplete()
    }

    @Test
    fun testResultObjectFailureWrongType() {
        Fuel.get("/ip").monoResultObject(IpLongDeserializer)
            .map(Result<IpLong, FuelError>::component2)
            .test()
            .assertNext { assertTrue(it?.exception is InvalidFormatException) }
            .verifyComplete()
    }

    @Test
    fun testResultObjectFailureMissingProperty() {
        Fuel.get("/ip").monoResultObject(IpAddressDeserializer)
            .map(Result<IpAddress, FuelError>::component2)
            .test()
            .assertNext { assertTrue(it?.exception is MissingKotlinParameterException) }
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
