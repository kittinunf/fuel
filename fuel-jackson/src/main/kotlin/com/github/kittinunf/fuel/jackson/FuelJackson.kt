package com.github.kittinunf.fuel.jackson

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.ResponseHandler
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.core.response
import com.github.kittinunf.result.Result
import java.io.InputStream
import java.io.Reader

val defaultMapper = ObjectMapper().registerKotlinModule()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

inline fun <reified T : Any> Request.responseObject(noinline handler: (Request, Response, Result<T, FuelError>) -> Unit) {
    response(jacksonDeserializerOf(), handler)
}

inline fun <reified T : Any> Request.responseObject(mapper: ObjectMapper, noinline handler: (Request, Response, Result<T, FuelError>) -> Unit) {
    response(jacksonDeserializerOf(mapper), handler)
}

inline fun <reified T : Any> Request.responseObject(mapper: ObjectMapper, handler: ResponseHandler<T>) = response(jacksonDeserializerOf(mapper), handler)

inline fun <reified T : Any> Request.responseObject(handler: ResponseHandler<T>) = response(jacksonDeserializerOf(), handler)

inline fun <reified T : Any> Request.responseObject() = response(jacksonDeserializerOf<T>())

inline fun <reified T : Any> Request.responseObject(mapper: ObjectMapper) = response(jacksonDeserializerOf<T>(mapper))

inline fun <reified T : Any> jacksonDeserializerOf(mapper: ObjectMapper = defaultMapper) = object : ResponseDeserializable<T> {
    override fun deserialize(reader: Reader): T? {
        return mapper.readValue(reader)
    }

    override fun deserialize(content: String): T? {
        return mapper.readValue(content)
    }

    override fun deserialize(bytes: ByteArray): T? {
        return mapper.readValue(bytes)
    }

    override fun deserialize(inputStream: InputStream): T? {
        return mapper.readValue(inputStream)
    }
}