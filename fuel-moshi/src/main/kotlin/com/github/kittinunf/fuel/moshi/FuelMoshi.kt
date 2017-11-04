package com.github.kittinunf.fuel.moshi

import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.result.Result
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi

inline fun <reified T : Any> Request.responseObject(noinline handler: (Request, Response, Result<T, FuelError>) -> Unit) =
        response(moshiDeserializerOf(), handler)

inline fun <reified T : Any> Request.responseObject(handler: Handler<T>) = response(moshiDeserializerOf(), handler)

fun Request.responseObject() = response(moshiDeserializerOf())

inline fun <reified T : Any> moshiDeserializerOf() = object : ResponseDeserializable<T> {
    override fun deserialize(content: String): T? =
            Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .build()
                    .adapter(T::class.java)
                    .fromJson(content)
}