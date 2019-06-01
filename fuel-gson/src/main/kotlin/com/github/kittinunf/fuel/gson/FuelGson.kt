package com.github.kittinunf.fuel.gson

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.core.ResponseHandler
import com.github.kittinunf.fuel.core.ResponseResultHandler
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.core.requests.CancellableRequest
import com.github.kittinunf.fuel.core.response
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Reader

/**
 * Asynchronously gets a response object of [T] wrapped in [Result] via [handler]
 *
 * @param handler [ResponseResultHandler<T>] the handler that is called upon success
 * @return [CancellableRequest] request that can be cancelled
 */
inline fun <reified T : Any> Request.responseObject(noinline handler: ResponseResultHandler<T>) =
    response(gsonDeserializer(), handler)

/**
 * Asynchronously gets a response object of [T] wrapped in [Result] via [handler] using custom [Gson] instance
 *
 * @param gson [Gson] custom Gson deserializer instance
 * @param handler [ResponseResultHandler<T>] the handler that is called upon success
 * @return [CancellableRequest] request that can be cancelled
 */
inline fun <reified T : Any> Request.responseObject(gson: Gson, noinline handler: ResponseResultHandler<T>) =
    response(gsonDeserializer(gson), handler)

/**
 * Asynchronously gets a response object of [T] or [com.github.kittinunf.fuel.core.FuelError] via [handler]
 *
 * @param handler [Handle<T>] the handler that is called upon success
 * @return [CancellableRequest] request that can be cancelled
 */
inline fun <reified T : Any> Request.responseObject(handler: ResponseHandler<T>) =
    response(gsonDeserializer(), handler)

/**
 * Asynchronously gets a response object of [T] or [com.github.kittinunf.fuel.core.FuelError] via [handler]
 *  using custom [Gson] instance
 *
 * @param gson [Gson] custom Gson deserializer instance
 * @param handler [Handle<T>] the handler that is called upon success
 * @return [CancellableRequest] request that can be cancelled
 */
inline fun <reified T : Any> Request.responseObject(gson: Gson, handler: ResponseHandler<T>) =
    response(gsonDeserializer(gson), handler)

/**
 * Synchronously get a response object of [T]
 *
 * @return [Triple<Request, Response, Result<T, FuelError>>] the deserialized result
 */
inline fun <reified T : Any> Request.responseObject() =
    response(gsonDeserializer<T>())

/**
 * Synchronously get a response object of [T]
 *
 * @param gson [Gson] custom Gson deserializer instance
 * @return [Triple<Request, Response, Result<T, FuelError>>] the deserialized result
 */
inline fun <reified T : Any> Request.responseObject(gson: Gson) =
    response(gsonDeserializer<T>(gson))

/**
 * Generate a [ResponseDeserializable<T>] that can deserialize json of [T]
 *
 * @return [ResponseDeserializable<T>] the deserializer
 */
inline fun <reified T : Any> gsonDeserializerOf(clazz: Class<T>) = object : ResponseDeserializable<T> {
    override fun deserialize(reader: Reader): T? = Gson().fromJson<T>(reader, clazz)
}

inline fun <reified T : Any> gsonDeserializer(gson: Gson) = object : ResponseDeserializable<T> {
    override fun deserialize(reader: Reader): T? = gson.fromJson<T>(reader, object : TypeToken<T>() {}.type)
}

inline fun <reified T : Any> gsonDeserializer() = gsonDeserializer<T>(Gson())

/**
 * Serializes [src] to json and sets the body as application/json
 *
 * @param src [Any] the source to serialize
 * @param gson [Gson] custom Gson deserializer instance
 * @return [Request] the modified request
 */
inline fun <reified T : Any> Request.jsonBody(src: T, gson: Gson) =
    this.jsonBody(gson.toJson(src, object : TypeToken<T>() {}.type)
        .also { Fuel.trace { "serialized $it" } } as String
    )

/**
 * Serializes [src] to json and sets the body as application/json
 *
 * @param src [Any] the source to serialize
 * @return [Request] the modified request
 */
inline fun <reified T : Any> Request.jsonBody(src: T) = this.jsonBody(src, Gson())
