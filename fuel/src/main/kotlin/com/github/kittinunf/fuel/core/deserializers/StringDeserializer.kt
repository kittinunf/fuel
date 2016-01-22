package com.github.kittinunf.fuel.core.deserializers

import com.github.kittinunf.fuel.core.Deserializable
import com.github.kittinunf.fuel.core.Response
import java.nio.charset.Charset

class StringDeserializer(val charset: Charset = Charsets.UTF_8) : Deserializable<String> {
    override fun deserialize(response: Response): String {
        return String(response.data, charset)
    }
}