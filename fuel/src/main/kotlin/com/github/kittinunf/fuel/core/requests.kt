package com.github.kittinunf.fuel.core

import com.github.kittinunf.result.Result

//byte array
fun Request.response(handler: (Request, Response, Result<ByteArray, FuelError>) -> Unit) =
        response(byteArrayDeserializer(), handler)

fun Request.response(handler: Handler<ByteArray>) = response(byteArrayDeserializer(), handler)

fun Request.response() = response(byteArrayDeserializer())

//string
fun Request.responseString(handler: (Request, Response, Result<String, FuelError>) -> Unit) =
        response(stringDeserializer(), handler)

fun Request.responseString(handler: Handler<String>) = response(stringDeserializer(), handler)

fun Request.responseString() = response(stringDeserializer())

//object
fun <T : Any> Request.responseObject(deserializer: ResponseDeserializable<T>, handler: (Request, Response, Result<T, FuelError>) -> Unit) = response(deserializer, handler)

fun <T : Any> Request.responseObject(deserializer: ResponseDeserializable<T>, handler: Handler<T>) = response(deserializer, handler)

fun <T : Any> Request.responseObject(deserializer: ResponseDeserializable<T>) = response(deserializer)


fun byteArrayDeserializer(): Deserializable<ByteArray> {
    return object : Deserializable<ByteArray> {
        override fun deserialize(response: Response): ByteArray {
            return response.data
        }
    }
}

fun stringDeserializer(): Deserializable<String> {
    return object : Deserializable<String> {
        override fun deserialize(response: Response): String {
            return String(response.data)
        }
    }
}