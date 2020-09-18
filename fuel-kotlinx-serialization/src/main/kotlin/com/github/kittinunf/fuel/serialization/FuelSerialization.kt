package com.github.kittinunf.fuel.serialization

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.core.ResponseHandler
import com.github.kittinunf.fuel.core.response
import com.github.kittinunf.result.Result
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.io.InputStream
import java.io.Reader

inline fun <reified T : Any> Request.responseObject(
    loader: DeserializationStrategy<T>,
    json: Json = Json { allowStructuredMapKeys = true },
    noinline deserializer: (Request, Response, Result<T, FuelError>) -> Unit
) = response(kotlinxDeserializerOf(loader, json), deserializer)

@OptIn(InternalSerializationApi::class)
inline fun <reified T : Any> Request.responseObject(
    json: Json = Json { allowStructuredMapKeys = true },
    noinline deserializer: (Request, Response, Result<T, FuelError>) -> Unit
) = responseObject(T::class.serializer(), json, deserializer)

inline fun <reified T : Any> Request.responseObject(
    deserializer: ResponseHandler<T>,
    loader: DeserializationStrategy<T>,
    json: Json = Json { allowStructuredMapKeys = true }
) = response(kotlinxDeserializerOf(loader, json), deserializer)

@OptIn(InternalSerializationApi::class)
inline fun <reified T : Any> Request.responseObject(
    deserializer: ResponseHandler<T>,
    json: Json = Json { allowStructuredMapKeys = true }
) = responseObject(deserializer, T::class.serializer(), json)

inline fun <reified T : Any> Request.responseObject(
    loader: DeserializationStrategy<T>,
    json: Json = Json { allowStructuredMapKeys = true }
) = response(kotlinxDeserializerOf(loader, json))

@OptIn(InternalSerializationApi::class)
inline fun <reified T : Any> Request.responseObject(
    json: Json = Json { allowStructuredMapKeys = true }
) = responseObject(T::class.serializer(), json)

inline fun <reified T : Any> kotlinxDeserializerOf(
    loader: DeserializationStrategy<T>,
    json: Json = Json { allowStructuredMapKeys = true }
) = object : ResponseDeserializable<T> {
    override fun deserialize(content: String): T? = json.decodeFromString(loader, content)
    override fun deserialize(reader: Reader): T? = deserialize(reader.readText())
    override fun deserialize(bytes: ByteArray): T? = deserialize(String(bytes))

    override fun deserialize(inputStream: InputStream): T? {
        inputStream.bufferedReader().use {
            return deserialize(it)
        }
    }
}

@OptIn(InternalSerializationApi::class)
inline fun <reified T : Any> kotlinxDeserializerOf(
    json: Json = Json { allowStructuredMapKeys = true }
) = kotlinxDeserializerOf(T::class.serializer(), json)
