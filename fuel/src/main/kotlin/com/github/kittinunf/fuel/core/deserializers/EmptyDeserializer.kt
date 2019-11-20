package com.github.kittinunf.fuel.core.deserializers

import com.github.kittinunf.fuel.core.Deserializable
import com.github.kittinunf.fuel.core.Response

object EmptyDeserializer : Deserializable<Unit> {
    override fun deserialize(response: Response) = Unit
}