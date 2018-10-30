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

private fun <T : Any> Request.rxResponse(deserializable: Deserializable<T>): Single<Pair<Response, Result<T, FuelError>>> =
    rx {
        val (_, response, result) = response(deserializable)
        response to result
    }

private fun <T : Any> Request.rxResult(deserializable: Deserializable<T>): Single<Result<T, FuelError>> =
    rx {
        val (_, _, result) = response(deserializable)
        result
    }

/**
 * Returns a reactive stream for a [Single] value response [ByteArray]
 *
 * @see rxBytes
 * @return [Single<Pair<Response, Result<ByteArray, FuelError>>>] the [ByteArray] wrapped into a [Pair] and [Result]
 */
fun Request.rxResponse() =
    rxResponse(ByteArrayDeserializer())

/**
 * Returns a reactive stream for a [Single] value response [String]
 *
 * @see rxString
 *
 * @param charset [Charset] the character set to deserialize with
 * @return [Single<Pair<Response, Result<String, FuelError>>>] the [String] wrapped into a [Pair] and [Result]
 */
fun Request.rxResponseString(charset: Charset = Charsets.UTF_8) =
    rxResponse(StringDeserializer(charset))

/**
 * Returns a reactive stream for a [Single] value response object [T]
 *
 * @see rxObject
 *
 * @param deserializable [Deserializable<T>] something that can deserialize the [Response] to a [T]
 * @return [Single<Pair<Response, Result<T, FuelError>>>] the [T] wrapped into a [Pair] and [Result]
 */
fun <T : Any> Request.rxResponseObject(deserializable: Deserializable<T>) =
    rxResponse(deserializable)

/**
 * Returns a reactive stream for a [Single] value response [ByteArray]
 *
 * @see rxResponse
 * @return [Single<Result<ByteArray, FuelError>>] the [ByteArray] wrapped into a [Result]
 */
fun Request.rxBytes() =
    rxResult(ByteArrayDeserializer())

/**
 * Returns a reactive stream for a [Single] value response [ByteArray]
 *
 * @see rxResponseString
 *
 * @param charset [Charset] the character set to deserialize with
 * @return [Single<Result<String, FuelError>>] the [String] wrapped into a [Result]
 */
fun Request.rxString(charset: Charset = Charsets.UTF_8) =
    rxResult(StringDeserializer(charset))

/**
 * Returns a reactive stream for a [Single] value response [T]
 *
 * @see rxResponseObject
 *
 * @param deserializable [Deserializable<T>] something that can deserialize the [Response] to a [T]
 * @return [Single<Result<T, FuelError>>] the [T] wrapped into a [Result]
 */
fun <T : Any> Request.rxObject(deserializable: Deserializable<T>) =
    rxResult(deserializable)

/**
 * Generic [Single] wrapper that executes [resultBlock] and emits its result [R] to the [Single]
 *
 * @param resultBlock [() -> R] function that returns [R]
 * @return [Single] the reactive stream for a [Single] with response [R]
 */
fun <R : Any> Request.rx(resultBlock: Request.() -> R): Single<R> =
    Single.create { emitter ->
        val result = resultBlock()
        emitter.onSuccess(result)
        emitter.setCancellable { this.cancel() }
    }
