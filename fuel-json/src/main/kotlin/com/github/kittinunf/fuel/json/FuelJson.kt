package com.github.kittinunf.fuel.json

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.core.ResponseHandler
import com.github.kittinunf.fuel.core.response
import com.github.kittinunf.result.Result
import org.json.JSONArray
import org.json.JSONObject

class FuelJson(val content: String) {
    fun obj(): JSONObject = JSONObject(content)
    fun array(): JSONArray = JSONArray(content)
}

fun Request.responseJson(handler: (Request, Response, Result<FuelJson, FuelError>) -> Unit) =
    response(jsonDeserializer(), handler)

fun Request.responseJson(handler: ResponseHandler<FuelJson>) = response(jsonDeserializer(), handler)

fun Request.responseJson() = response(jsonDeserializer())

fun jsonDeserializer() = object : ResponseDeserializable<FuelJson> {
    override fun deserialize(response: Response): FuelJson = FuelJson(String(response.data))
}
