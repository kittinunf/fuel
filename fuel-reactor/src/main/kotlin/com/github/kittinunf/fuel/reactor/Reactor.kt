package com.github.kittinunf.fuel.reactor

import com.github.kittinunf.fuel.core.Deserializable
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.deserializers.ByteArrayDeserializer
import com.github.kittinunf.fuel.core.deserializers.StringDeserializer
import com.github.kittinunf.fuel.core.response
import reactor.core.publisher.Mono
import java.nio.charset.Charset

fun <T : Any> Request.mono(mapper: Deserializable<T>): Mono<T> =
    Mono.create<T> { sink ->
        sink.onCancel(this::cancel)
        val (_, _, result) = response(mapper)
        result.fold(sink::success, sink::error)
    }

fun Request.monoOfBytes(): Mono<ByteArray> =
    mono(ByteArrayDeserializer())

fun Request.monoOfString(charset: Charset = Charsets.UTF_8): Mono<String> =
    mono(StringDeserializer(charset))
