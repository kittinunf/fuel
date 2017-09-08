package com.github.kittiunf.fuel.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.result.Result
import java.io.InputStream
import java.io.Reader

val mapper : ObjectMapper = ObjectMapper().registerKotlinModule()

inline fun <reified T : Any> Request.responseObject(noinline handler: (Request, Response, Result<T, FuelError>) -> Unit) {
    response(jacksonDeserializerOf(), handler)
}

inline fun <reified T : Any> Request.responseObject(handler: Handler<T>) = response(jacksonDeserializerOf(), handler)

inline fun <reified T : Any> Request.responseObject() = response(jacksonDeserializerOf())


inline fun <reified T : Any> jacksonDeserializerOf() = object : ResponseDeserializable<T> {
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

    override fun deserialize(response: Response): T {
        return mapper.readValue(response.dataStream)
    }
}
