package com.github.kittinunf.fuel.livedata

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.fuel.core.deserializers.ByteArrayDeserializer
import com.github.kittinunf.fuel.core.deserializers.StringDeserializer
import com.github.kittinunf.result.Result
import java.nio.charset.Charset

/**
 * Created by Ihor Kucherenko on 01.06.17.
 * https://github.com/KucherenkoIhor
 */
fun Request.liveData_response() = liveData_response(ByteArrayDeserializer())

fun Request.liveData_responseString(charset: Charset = Charsets.UTF_8) = liveData_response(StringDeserializer(charset))

fun <T : Any> Request.liveData_responseObject(deserializable: Deserializable<T>) = liveData_response(deserializable)

private fun <T : Any> Request.liveData_response(deserializable: Deserializable<T>): LiveData<Pair<Response, Result<T, FuelError>>> {
    val liveData = MutableLiveData<Pair<Response, Result<T, FuelError>>>()
    val handler: (Request, Response, Result<T, FuelError>) -> Unit = { request, response, result ->
        liveData.value = response to result
    }
    response(deserializable, handler)
    return liveData
}

fun Request.liveData_bytes() = liveData_result(ByteArrayDeserializer())

fun Request.liveData_string(charset: Charset = Charsets.UTF_8) = liveData_result(StringDeserializer(charset))

fun <T : Any> Request.liveData_object(deserializable: Deserializable<T>) = liveData_result(deserializable)

private fun <T : Any> Request.liveData_result(deserializable: Deserializable<T>): LiveData<Result<T, FuelError>> {
    val liveData = MutableLiveData<Result<T, FuelError>>()
    val handler: (Request, Response, Result<T, FuelError>) -> Unit = { request, response, result ->
        liveData.value = result
    }
    response(deserializable, handler)
    return liveData
}