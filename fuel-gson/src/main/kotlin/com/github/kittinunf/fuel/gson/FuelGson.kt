package com.github.kittinunf.fuel.gson

import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Reader

/**
 * Created by ihor_kucherenko on 7/4/17.
 * https://github.com/KucherenkoIhor
 */
class GsonDeserializer<out T : Any> : ResponseDeserializable<T> {

    override fun deserialize(reader: Reader): T
            = Gson().fromJson<T>(reader, object : TypeToken<T>() {}.type)

}

fun <T : Any> Request.responseObject(handler: (Request, Response, Result<T, FuelError>) -> Unit) = response(GsonDeserializer<T>(), handler)

fun <T : Any> Request.responseObject(handler: Handler<T>) = response(GsonDeserializer<T>(), handler)

fun <T : Any> Request.responseObject() = response(GsonDeserializer<T>())

