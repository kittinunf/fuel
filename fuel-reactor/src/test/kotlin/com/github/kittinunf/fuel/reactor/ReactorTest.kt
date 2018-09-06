package com.github.kittinunf.fuel.reactor

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.ResponseDeserializable
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
    fun streamBytes() {
        Fuel.get("/ip").monoOfBytes()
            .test()
            .assertNext { assertTrue(it.size > 0) }
            .verifyComplete()
    }

    @Test
    fun streamString() {
        Fuel.get("/uuid").monoOfString()
            .test()
            .assertNext { assertTrue(it.isNotEmpty()) }
            .verifyComplete()
    }

    private data class Ip(val origin: String)

    private object IpDeserializerSuccess : ResponseDeserializable<Ip> {
        override fun deserialize(content: String) = jacksonObjectMapper().readValue<Ip>(content)
    }

    @Test
    fun streamObjectSuccess() {
        Fuel.get("/ip").monoOfObject(IpDeserializerSuccess)
            .map(Ip::origin)
            .test()
            .assertNext { assertTrue(it.isNotEmpty()) }
            .verifyComplete()
    }

    private data class IpLong(val origin: Long)

    private object IpLongDeserializer : ResponseDeserializable<IpLong> {
        override fun deserialize(content: String) = jacksonObjectMapper().readValue<IpLong>(content)
    }

    @Test
    fun streamObjectFailureWrongType() {
        Fuel.get("/ip").monoOfObject(IpLongDeserializer)
            .map(IpLong::origin)
            .test()
            .expectError(InvalidFormatException::class.java)
            .verify()
    }

    private data class IpAddress(val address: String)

    private object IpAddressDeserializer : ResponseDeserializable<IpAddress> {
        override fun deserialize(content: String) = jacksonObjectMapper().readValue<IpAddress>(content)
    }

    @Test
    fun streamObjectFailureMissingProperty() {
        val errorMessage = Fuel.get("/ip").monoOfObject(IpAddressDeserializer)
            .map(IpAddress::address)
            .onErrorResume(MissingKotlinParameterException::class, { Mono.just(it.message!!) })
            .block()!!

        assertTrue(errorMessage.contains("value failed for JSON property address due to missing"))
    }
}
