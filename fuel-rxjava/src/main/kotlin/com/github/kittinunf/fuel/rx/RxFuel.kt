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

fun Request.toRxResponse() = toRxResponse(ByteArrayDeserializer())

fun Request.toRxResponseString(charset: Charset = Charsets.UTF_8) = toRxResponse(StringDeserializer(charset))

fun <T : Any> Request.toRxResponseObject(deserializable: Deserializable<T>) = toRxResponse(deserializable)

private fun <T : Any> Request.toRxResponse(deserializable: Deserializable<T>): Single<Pair<Response, Result<T, FuelError>>> =
        rx {
            val (_, response, result) = response(deserializable)
            response to result
        }

fun Request.toRxBytes() = toRxResult(ByteArrayDeserializer())

fun Request.toRxString(charset: Charset = Charsets.UTF_8) = toRxResult(StringDeserializer(charset))

fun <T : Any> Request.toRxObject(deserializable: Deserializable<T>) = toRxResult(deserializable)

private fun <T : Any> Request.toRxResult(deserializable: Deserializable<T>): Single<Result<T, FuelError>> =
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
