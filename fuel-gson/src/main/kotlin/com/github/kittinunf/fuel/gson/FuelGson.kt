package com.github.kittinunf.fuel.gson

import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Reader
import java.lang.reflect.Type

/**
 * Created by ihor_kucherenko on 7/4/17.
 * https://github.com/KucherenkoIhor
 *
 * Note: Use the default constructor iff no generics
 */
class GsonDeserializer<out T : Any>(private val type: Type = (object : TypeToken<T>(){} as TypeToken<*>).type) : ResponseDeserializable<T> {

    override fun deserialize(reader: Reader): T = Gson().fromJson<T>(reader, type)

}

inline fun <reified T : Any> Request.responseObject(noinline handler: (Request, Response, Result<T, FuelError>) -> Unit) =
        response(GsonDeserializer<T>(object : TypeToken<T>(){}.type), handler)

inline fun <reified T : Any> Request.responseObject(handler: Handler<T>) = response(GsonDeserializer<T>(object : TypeToken<T>(){}.type), handler)

inline fun <reified T : Any> Request.responseObject() = response(GsonDeserializer<T>(object : TypeToken<T>(){}.type))


