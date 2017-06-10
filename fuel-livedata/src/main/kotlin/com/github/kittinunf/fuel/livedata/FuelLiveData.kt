package com.github.kittinunf.fuel.livedata

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.github.kittinunf.fuel.core.Deserializable
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.deserializers.ByteArrayDeserializer
import com.github.kittinunf.fuel.core.deserializers.StringDeserializer
import com.github.kittinunf.fuel.core.response
import com.github.kittinunf.result.Result
import java.nio.charset.Charset

/**
 * Created by Ihor Kucherenko on 01.06.17.
 * https://github.com/KucherenkoIhor
 */
fun Request.liveDataResponse() = liveDataResponse(ByteArrayDeserializer())

fun Request.liveDataResponseString(charset: Charset = Charsets.UTF_8) = liveDataResponse(StringDeserializer(charset))

fun <T : Any> Request.liveDataResponseObject(deserializable: Deserializable<T>) = liveDataResponse(deserializable)

private fun <T : Any> Request.liveDataResponse(deserializable: Deserializable<T>): LiveData<Pair<Response, Result<T, FuelError>>> {
    val liveData = MutableLiveData<Pair<Response, Result<T, FuelError>>>()
    val handler: (Request, Response, Result<T, FuelError>) -> Unit = { _, response, result ->
        liveData.value = response to result
    }
    response(deserializable, handler)
    return liveData
}

fun Request.liveDataBytes() = liveDataResult(ByteArrayDeserializer())

fun Request.liveDataString(charset: Charset = Charsets.UTF_8) = liveDataResult(StringDeserializer(charset))

fun <T : Any> Request.liveDataObject(deserializable: Deserializable<T>) = liveDataResult(deserializable)

private fun <T : Any> Request.liveDataResult(deserializable: Deserializable<T>): LiveData<Result<T, FuelError>> {
    val liveData = MutableLiveData<Result<T, FuelError>>()
    val handler: (Request, Response, Result<T, FuelError>) -> Unit = { _, _, result ->
        liveData.value = result
    }
    response(deserializable, handler)
    return liveData
}