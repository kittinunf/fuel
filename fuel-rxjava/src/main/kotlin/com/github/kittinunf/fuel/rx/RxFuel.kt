
package com.github.kittinunf.fuel.rx

import com.github.kittinunf.fuel.core.Deserializable
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.deserializers.ByteArrayDeserializer
import com.github.kittinunf.fuel.core.deserializers.StringDeserializer
import com.github.kittinunf.fuel.core.requests.CancellableRequest
import com.github.kittinunf.fuel.core.response
import com.github.kittinunf.result.Result
import io.reactivex.Single
import java.nio.charset.Charset

private fun <T : Any> Request.rxResponseSingle(deserializable: Deserializable<T>): Single<T> =
    rx { onSuccess, onFailure ->
        response(deserializable) { _, _, result ->
            result.fold(
                success = { t -> t.also { onSuccess(t) } },
                failure = { e -> onFailure(e) }
            )
        }
    }

private fun <T : Any> Request.rxResponsePair(deserializable: Deserializable<T>): Single<Pair<Response, T>> =
    rx { onSuccess, onFailure ->
        response(deserializable) { _, response, result ->
            result.fold(
                    success = { t -> t.also { onSuccess(Pair(response, t)) } },
                    failure = { e -> onFailure(e) }
            )
        }
    }

private fun <T : Any> Request.rxResponseTriple(deserializable: Deserializable<T>): Single<Triple<Request, Response, T>> =
    rx { onSuccess, onFailure ->
        response(deserializable) { request, response, result ->
            result.fold(
                success = { t -> t.also { onSuccess(Triple(request, response, t)) } },
                failure = { e -> onFailure(e) }
            )
        }
    }

private fun <T : Any> Request.rxResultSingle(deserializable: Deserializable<T>): Single<Result<T, FuelError>> =
    rx { onSuccess ->
        response(deserializable) { _, _, result ->
            onSuccess(result)
        }
    }

private fun <T : Any> Request.rxResultPair(deserializable: Deserializable<T>): Single<Pair<Response, Result<T, FuelError>>> =
    rx { onSuccess ->
        response(deserializable) { _, response, result ->
            onSuccess(response to result)
        }
    }

private fun <T : Any> Request.rxResultTriple(deserializable: Deserializable<T>): Single<Triple<Request, Response, Result<T, FuelError>>> =
    rx { onSuccess ->
        response(deserializable) { request, response, result ->
            onSuccess(Triple(request, response, result))
        }
    }

/**
 * Returns a reactive stream for a [Single] value response [ByteArray]
 *
 * @see rxBytes
 * @return [Single<T>] the [ByteArray] wrapped into a [Pair] and [Result]
 */
fun Request.rxResponse() = rxResponseSingle(ByteArrayDeserializer())
fun Request.rxResponsePair() = rxResponsePair(ByteArrayDeserializer())
fun Request.rxResponseTriple() = rxResponseTriple(ByteArrayDeserializer())

/**
 * Returns a reactive stream for a [Single] value response [String]
 *
 * @see rxString
 *
 * @param charset [Charset] the character set to deserialize with
 * @return [Single<String>] the [String] wrapped into a [Pair] and [Result]
 */
fun Request.rxResponseString(charset: Charset = Charsets.UTF_8) = rxResponseSingle(StringDeserializer(charset))
fun Request.rxResponseStringPair(charset: Charset = Charsets.UTF_8) = rxResponsePair(StringDeserializer(charset))
fun Request.rxResponseStringTriple(charset: Charset = Charsets.UTF_8) = rxResponseTriple(StringDeserializer(charset))

/**
 * Returns a reactive stream for a [Single] value response object [T]
 *
 * @see rxObject
 *
 * @param deserializable [Deserializable<T>] something that can deserialize the [Response] to a [T]
 * @return [Single<T>] the [T] wrapped into a [Pair] and [Result]
 */
fun <T : Any> Request.rxResponseObject(deserializable: Deserializable<T>) = rxResponseSingle(deserializable)
fun <T : Any> Request.rxResponseObjectPair(deserializable: Deserializable<T>) = rxResponsePair(deserializable)
fun <T : Any> Request.rxResponseObjectTriple(deserializable: Deserializable<T>) = rxResponseTriple(deserializable)

/**
 * Returns a reactive stream for a [Single] value response [ByteArray]
 *
 * @see rxResponse
 * @return [Single<Result<ByteArray, FuelError>>] the [ByteArray] wrapped into a [Result]
 */
fun Request.rxBytes() = rxResultSingle(ByteArrayDeserializer())
fun Request.rxBytesPair() = rxResultPair(ByteArrayDeserializer())
fun Request.rxBytesTriple() = rxResultTriple(ByteArrayDeserializer())

/**
 * Returns a reactive stream for a [Single] value response [ByteArray]
 *
 * @see rxResponseString
 *
 * @param charset [Charset] the character set to deserialize with
 * @return [Single<Result<String, FuelError>>] the [String] wrapped into a [Result]
 */
fun Request.rxString(charset: Charset = Charsets.UTF_8) = rxResultSingle(StringDeserializer(charset))
fun Request.rxStringPair(charset: Charset = Charsets.UTF_8) = rxResultPair(StringDeserializer(charset))
fun Request.rxStringTriple(charset: Charset = Charsets.UTF_8) = rxResultTriple(StringDeserializer(charset))

/**
 * Returns a reactive stream for a [Single] value response [T]
 *
 * @see rxResponseObject
 *
 * @param deserializable [Deserializable<T>] something that can deserialize the [Response] to a [T]
 * @return [Single<Result<T, FuelError>>] the [T] wrapped into a [Result]
 */
fun <T : Any> Request.rxObject(deserializable: Deserializable<T>) = rxResultSingle(deserializable)
fun <T : Any> Request.rxObjectPair(deserializable: Deserializable<T>) = rxResultPair(deserializable)
fun <T : Any> Request.rxObjectTriple(deserializable: Deserializable<T>) = rxResultTriple(deserializable)

/**
 * Generic [Single] wrapper that executes [resultBlock] and emits its result [R] to the [Single]
 *
 * This wrapper is a [io.reactivex.Single] wrapper that uses onError to signal the error that occurs
 * in the stream. If you wish to receive an Error in the format of [com.github.kittinunf.result.Result],
 * please use [rx] instead.
 *
 * @param resultBlock [() -> R] function that returns [R]
 * @return [Single] the reactive stream for a [Single] with response [R]
 */
fun <R : Any> Request.rx(resultBlock: Request.((R) -> Unit, (Throwable) -> Unit) -> CancellableRequest): Single<R> =
    Single.create { emitter ->
        val cancellableRequest = resultBlock(emitter::onSuccess, emitter::onError)
        emitter.setCancellable { cancellableRequest.cancel() }
    }

/**
 * Generic [Single] wrapper that executes [resultBlock] and emits its result [R] to the [Single]
 *
 * This wrapper is a [io.reactivex.Single] wrapper that uses onSuccess in the format of [com.github.kittinunf.result.Result]
 * as a value in the stream.
 *
 * @param resultBlock [() -> R] function that returns [R]
 * @return [Single] the reactive stream for a [Single] with response [R]
 */
fun <R : Any> Request.rx(resultBlock: Request.((R) -> Unit) -> CancellableRequest): Single<R> =
    Single.create { emitter ->
        val cancellableRequest = resultBlock(emitter::onSuccess)
        emitter.setCancellable { cancellableRequest.cancel() }
    }

@Suppress("FunctionName")
@Deprecated(
    "Use Request.rxResponsePair",
    replaceWith = ReplaceWith("rxResponsePair()"),
    level = DeprecationLevel.ERROR
)
fun Request.rx_response() = rxResponsePair()

@Suppress("FunctionName")
@Deprecated(
    "Use Request.rxResponseStringPair",
    replaceWith = ReplaceWith("rxResponseStringPair(charset)"),
    level = DeprecationLevel.ERROR
)
fun Request.rx_responseString(charset: Charset = Charsets.UTF_8) = rxResponseStringPair(charset)

@Suppress("FunctionName")
@Deprecated(
    "Use Request.rxResponseObjectPair",
    replaceWith = ReplaceWith("rxResponseObjectPair(deserializable)"),
    level = DeprecationLevel.ERROR
)
fun <T : Any> Request.rx_responseObject(deserializable: Deserializable<T>) = rxResponseObjectPair(deserializable)

@Suppress("FunctionName")
@Deprecated(
    "Use Request.rxBytes",
    replaceWith = ReplaceWith("rxBytes()"),
    level = DeprecationLevel.ERROR
)
fun Request.rx_bytes() = rxBytes()

@Suppress("FunctionName")
@Deprecated(
    "Use Request.rxString",
    replaceWith = ReplaceWith("rxString(charset)"),
    level = DeprecationLevel.ERROR
)
fun Request.rx_string(charset: Charset = Charsets.UTF_8) = rxString(charset)

@Suppress("FunctionName")
@Deprecated(
    "Use Request.rxObject",
    replaceWith = ReplaceWith("rxObject(deserializable)"),
    level = DeprecationLevel.ERROR
)
fun <T : Any> Request.rx_object(deserializable: Deserializable<T>) = rxObject(deserializable)
