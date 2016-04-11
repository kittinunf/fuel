package com.github.kittinunf.fuel.core.deserializers

import com.github.kittinunf.fuel.core.Deserializable
import com.github.kittinunf.fuel.core.Response

class ByteArrayDeserializer : Deserializable<ByteArray> {
    override fun deserialize(response: Response): ByteArray {
        return response.data
    }
}