package com.github.kittinunf.fuel.rx

import com.github.kittinunf.fuel.core.Deserializable
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.deserializers.ByteArrayDeserializer
import com.github.kittinunf.fuel.core.deserializers.StringDeserializer
import com.github.kittinunf.fuel.core.response
import com.github.kittinunf.result.Result
import rx.Observable
import rx.subjects.AsyncSubject
import java.nio.charset.Charset

fun Request.rx_response() = rx_response(ByteArrayDeserializer())

fun Request.rx_responseString(charset: Charset = Charsets.UTF_8) = rx_response(StringDeserializer(charset))

fun <T : Any> Request.rx_responseObject(deserializable: Deserializable<T>) = rx_response(deserializable)

private fun <T : Any> Request.rx_response(deserializable: Deserializable<T>): Observable<Pair<Response, T>> =
        Observable.defer {
            val source = AsyncSubject.create<Pair<Response, T>>()
            response(deserializable) { request, response, result ->
                when (result) {
                    is Result.Success -> {
                        source.onNext(response to result.value)
                        source.onCompleted()
                    }

                    is Result.Failure -> {
                        source.onError(result.error)
                    }
                }
            }
            source.doOnUnsubscribe { this.cancel() }
        }

fun Request.rx_bytes() = rx_result(ByteArrayDeserializer())

fun Request.rx_string(charset: Charset = Charsets.UTF_8) = rx_result(StringDeserializer(charset))

fun <T : Any> Request.rx_object(deserializable: Deserializable<T>) = rx_result(deserializable)

private fun <T : Any> Request.rx_result(deserializable: Deserializable<T>): Observable<T> =
        Observable.defer {
            val source = AsyncSubject.create<T>()
            response(deserializable) { request, response, result ->
                when (result) {
                    is Result.Success -> {
                        source.onNext(result.value)
                        source.onCompleted()
                    }

                    is Result.Failure -> {
                        source.onError(result.error)
                    }
                }
            }
            source.doOnUnsubscribe { this.cancel() }
        }

