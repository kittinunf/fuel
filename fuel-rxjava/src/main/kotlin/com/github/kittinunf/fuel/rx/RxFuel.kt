package com.github.kittinunf.fuel.rx

import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.fuel.core.deserializers.ByteArrayDeserializer
import com.github.kittinunf.fuel.core.deserializers.StringDeserializer
import com.github.kittinunf.result.Result
import io.reactivex.Single
import java.nio.charset.Charset

fun Request.rx_response() = rx_response(ByteArrayDeserializer())

fun Request.rx_responseString(charset: Charset = Charsets.UTF_8) = rx_response(StringDeserializer(charset))

fun <T : Any> Request.rx_responseObject(deserializable: Deserializable<T>) = rx_response(deserializable)

private fun <T : Any> Request.rx_response(deserializable: Deserializable<T>): Single<Pair<Response, Result<T, FuelError>>> =
        Single.create { emitter ->
            response(deserializable) { _, response, result ->
                emitter.onSuccess(response to result)
            }
            emitter.setCancellable { this.cancel() }
        }

fun Request.rx_bytes() = rx_result(ByteArrayDeserializer())

fun Request.rx_string(charset: Charset = Charsets.UTF_8) = rx_result(StringDeserializer(charset))

fun <T : Any> Request.rx_object(deserializable: Deserializable<T>) = rx_result(deserializable)

private fun <T : Any> Request.rx_result(deserializable: Deserializable<T>): Single<Result<T, FuelError>> =
        Single.create { emitter ->
            response(deserializable) { _, _, result ->
                emitter.onSuccess(result)
            }
            emitter.setCancellable { this.cancel() }
        }

