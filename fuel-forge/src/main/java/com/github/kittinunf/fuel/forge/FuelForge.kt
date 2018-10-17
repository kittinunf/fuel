package com.github.kittinunf.fuel.forge

import com.github.kittinunf.forge.Forge
import com.github.kittinunf.forge.core.DeserializedResult
import com.github.kittinunf.forge.core.JSON
import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.result.Result

inline fun <reified T : Any> Request.responseObject(noinline deserializer: JSON.() -> DeserializedResult<T>, noinline handler: (Request, Response, Result<T, FuelError>) -> Unit) =
        response(forgeDeserializerOf(deserializer), handler)

inline fun <reified T : Any> Request.responseObjects(noinline deserializer: JSON.() -> DeserializedResult<T>, noinline handler: (Request, Response, Result<List<T>, FuelError>) -> Unit) =
        response(forgesDeserializerOf(deserializer), handler)

inline fun <reified T : Any> Request.responseObject(noinline deserializer: JSON.() -> DeserializedResult<T>, handler: Handler<T>) =
        response(forgeDeserializerOf(deserializer), handler)

inline fun <reified T : Any> Request.responseObjects(noinline deserializer: JSON.() -> DeserializedResult<T>, handler: Handler<List<T>>) =
        response(forgesDeserializerOf(deserializer), handler)

inline fun <reified T : Any> Request.responseObject(noinline deserializer: JSON.() -> DeserializedResult<T>) = response(forgeDeserializerOf(deserializer))

inline fun <reified T : Any> Request.responseObjects(noinline deserializer: JSON.() -> DeserializedResult<T>) = response(forgesDeserializerOf(deserializer))

fun <T : Any> forgeDeserializerOf(deserializer: JSON.() -> DeserializedResult<T>) = object : ResponseDeserializable<T> {
    override fun deserialize(content: String): T? = Forge.modelFromJson(content, deserializer).component1()
}
fun <T : Any> forgesDeserializerOf(deserializer: JSON.() -> DeserializedResult<T>) = object : ResponseDeserializable<List<T>> {
    override fun deserialize(content: String): List<T>? = Forge.modelsFromJson(content, deserializer).map { it.get<T>() }
}