package com.github.kittinunf.fuel.android.extension

import com.github.kittinunf.fuel.android.core.Json
import com.github.kittinunf.fuel.core.Deserializable
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Handler
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.response
import com.github.kittinunf.result.Result

// jsonObject
fun Request.responseJson(handler: (Request, Response, Result<Json, FuelError>) -> Unit) =
        response(jsonDeserializer(), handler)

fun Request.responseJson(handler: Handler<Json>) = response(jsonDeserializer(), handler)

fun Request.responseJson() = response(jsonDeserializer())

fun jsonDeserializer(): Deserializable<Json> {
    return object : Deserializable<Json> {
        override fun deserialize(response: Response): Json {
            return Json(String(response.data))
        }
    }
}
