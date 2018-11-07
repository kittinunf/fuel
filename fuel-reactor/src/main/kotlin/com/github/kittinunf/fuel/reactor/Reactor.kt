package com.github.kittinunf.fuel.reactor

import com.github.kittinunf.fuel.core.Deserializable
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.deserializers.ByteArrayDeserializer
import com.github.kittinunf.fuel.core.deserializers.StringDeserializer
import com.github.kittinunf.fuel.core.requests.CancellableRequest
import com.github.kittinunf.fuel.core.response
import com.github.kittinunf.result.Result
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink
import java.nio.charset.Charset

private fun <T : Any> Request.monoResult(async: Request.(MonoSink<T>) -> CancellableRequest): Mono<T> =
    Mono.create<T> { sink ->
        val cancellableRequest = async(sink)
        sink.onCancel { cancellableRequest.cancel() }
    }

private fun <T : Any> Request.monoResultFold(mapper: Deserializable<T>): Mono<T> =
    monoResult { sink ->
        response(mapper) { _, _, result ->
            result.fold(sink::success, sink::error)
        }
    }

fun Request.monoBytes(): Mono<ByteArray> =
    monoResultFold(ByteArrayDeserializer())

fun Request.monoString(charset: Charset = Charsets.UTF_8): Mono<String> =
    monoResultFold(StringDeserializer(charset))

fun <T : Any> Request.monoObject(mapper: Deserializable<T>): Mono<T> =
    monoResultFold(mapper)

private fun <T : Any> Request.monoResultUnFolded(mapper: Deserializable<T>): Mono<Result<T, FuelError>> =
    monoResult { sink ->
        response(mapper) { _, _, result ->
            sink.success(result)
        }
    }

fun Request.monoResultBytes(): Mono<Result<ByteArray, FuelError>> =
    monoResultUnFolded(ByteArrayDeserializer())

fun Request.monoResultString(charset: Charset = Charsets.UTF_8): Mono<Result<String, FuelError>> =
    monoResultUnFolded(StringDeserializer(charset))

fun <T : Any> Request.monoResultObject(mapper: Deserializable<T>): Mono<Result<T, FuelError>> =
    monoResultUnFolded(mapper)

fun Request.monoResponse(): Mono<Response> =
    monoResult { sink ->
        response { _, res, _ -> sink.success(res) }
    }
