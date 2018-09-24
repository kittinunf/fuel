package com.github.kittinunf.fuel.reactor

import com.github.kittinunf.fuel.core.Deserializable
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.deserializers.ByteArrayDeserializer
import com.github.kittinunf.fuel.core.deserializers.StringDeserializer
import com.github.kittinunf.fuel.core.response
import com.github.kittinunf.result.Result
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink
import java.nio.charset.Charset

private fun <T : Any> Request.monoOfResult(block: Request.(MonoSink<T>) -> Unit): Mono<T> =
    Mono.create<T> { sink ->
        sink.onCancel(this::cancel)
        block(sink)
    }

private fun <T : Any> Request.monoOfResultFold(mapper: Deserializable<T>): Mono<T> =
    monoOfResult { sink ->
        response(mapper) { _, _, result ->
            result.fold(sink::success, sink::error)
        }
    }

fun Request.monoOfBytes(): Mono<ByteArray> =
    monoOfResultFold(ByteArrayDeserializer())

fun Request.monoOfString(charset: Charset = Charsets.UTF_8): Mono<String> =
    monoOfResultFold(StringDeserializer(charset))

fun <T : Any> Request.monoOfObject(mapper: Deserializable<T>): Mono<T> =
    monoOfResultFold(mapper)

private fun <T : Any> Request.monoOfResultUnFolded(mapper: Deserializable<T>): Mono<Result<T, FuelError>> =
    monoOfResult { sink ->
        response(mapper) { _, _, result ->
            sink.success(result)
        }
    }

fun Request.monoOfResultBytes(): Mono<Result<ByteArray, FuelError>> =
    monoOfResultUnFolded(ByteArrayDeserializer())

fun Request.monoOfResultString(charset: Charset = Charsets.UTF_8): Mono<Result<String, FuelError>> =
    monoOfResultUnFolded(StringDeserializer(charset))

fun <T : Any> Request.monoOfResultObject(mapper: Deserializable<T>): Mono<Result<T, FuelError>> =
    monoOfResultUnFolded(mapper)

fun Request.monoOfResponse(): Mono<Response> =
    monoOfResult { sink ->
        response { _, res, _ -> sink.success(res) }
    }
