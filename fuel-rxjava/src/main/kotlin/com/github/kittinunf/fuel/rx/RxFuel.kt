package com.github.kittinunf.fuel.rx

import com.github.kittinunf.fuel.core.Deserializable
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.deserializers.ByteArrayDeserializer
import com.github.kittinunf.fuel.core.deserializers.StringDeserializer
import com.github.kittinunf.fuel.core.response
import com.github.kittinunf.result.Result
import rx.Observable
import rx.Subscription
import rx.subscriptions.BooleanSubscription
import java.nio.charset.Charset

fun Request.rx_response() = rx_response(ByteArrayDeserializer)

fun Request.rx_responseString(charset: Charset) = rx_response(StringDeserializer(charset))

fun <T : Any> Request.rx_responseObject(deserializable: Deserializable<T>) = rx_response(deserializable)

private fun <T : Any> Request.rx_response(deserializable: Deserializable<T>): Observable<Pair<Response, T>> =
        Observable.create { subscriber ->
            response(deserializable) { request, response, result ->
                when (result) {
                    is Result.Success -> {
                        subscriber.onNext(response to result.value)
                        subscriber.onCompleted()
                    }

                    is Result.Failure -> {
                        subscriber.onError(result.error)
                    }
                }
            }

            subscriber.add(createRequestSubscription(this))
        }

fun Request.rx_bytes() = rx_result(ByteArrayDeserializer)

fun Request.rx_string(charset: Charset) = rx_result(StringDeserializer(charset))

fun <T : Any> Request.rx_object(deserializable: Deserializable<T>) = rx_result(deserializable)

private fun <T : Any> Request.rx_result(deserializable: Deserializable<T>): Observable<T> =
        Observable.create { subscriber ->
            response(deserializable) { request, response, result ->
                when (result) {
                    is Result.Success -> {
                        subscriber.onNext(result.value)
                        subscriber.onCompleted()
                    }

                    is Result.Failure -> {
                        subscriber.onError(result.error)
                    }
                }
            }

            subscriber.add(createRequestSubscription(this))
        }

private fun createRequestSubscription(request: Request) = object : Subscription {

    val subscription = BooleanSubscription.create { request.cancel() }

    override fun isUnsubscribed() = subscription.isUnsubscribed

    override fun unsubscribe() {
        subscription.unsubscribe()
    }

}