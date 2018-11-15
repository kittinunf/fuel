package com.github.kittinunf.fuel.core.deserializers

import com.github.kittinunf.fuel.core.Deserializable
import com.github.kittinunf.fuel.core.Json
import com.github.kittinunf.fuel.core.Response

class JsonDeserializer() : Deserializable<Json> {
    override fun deserialize(response: Response): Json = Json(String(response.data))
}