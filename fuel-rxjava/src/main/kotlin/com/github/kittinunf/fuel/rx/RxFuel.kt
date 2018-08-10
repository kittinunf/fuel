package com.github.kittinunf.fuel.rx

import com.github.kittinunf.fuel.core.Deserializable
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.deserializers.ByteArrayDeserializer
import com.github.kittinunf.fuel.core.deserializers.StringDeserializer
import com.github.kittinunf.fuel.core.response
import com.github.kittinunf.result.Result
import io.reactivex.Single
import java.nio.charset.Charset

fun Request.rx_response() = rx_response(ByteArrayDeserializer())

fun Request.rx_responseString(charset: Charset = Charsets.UTF_8) = rx_response(StringDeserializer(charset))

fun <T : Any> Request.rx_responseObject(deserializable: Deserializable<T>) = rx_response(deserializable)

private fun <T : Any> Request.rx_response(deserializable: Deserializable<T>): Single<Pair<Response, Result<T, FuelError>>> =
        rx {
            val (_, response, result) = response(deserializable)
            response to result
        }

fun Request.rx_bytes() = rx_result(ByteArrayDeserializer())

fun Request.rx_string(charset: Charset = Charsets.UTF_8) = rx_result(StringDeserializer(charset))

fun <T : Any> Request.rx_object(deserializable: Deserializable<T>) = rx_result(deserializable)

private fun <T : Any> Request.rx_result(deserializable: Deserializable<T>): Single<Result<T, FuelError>> =
        rx {
            val (_, _, result) = response(deserializable)
            result
        }

fun <R : Any> Request.rx(resultBlock: Request.() -> R): Single<R> =
        Single.create { emitter ->
            val result = resultBlock()
            emitter.onSuccess(result)
            emitter.setCancellable { this.cancel() }
        }
