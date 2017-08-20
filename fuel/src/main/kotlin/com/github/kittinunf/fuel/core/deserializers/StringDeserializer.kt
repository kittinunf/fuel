package com.github.kittinunf.fuel.core.deserializers

import com.github.kittinunf.fuel.core.Deserializable
import com.github.kittinunf.fuel.core.Response
import java.nio.charset.Charset

class StringDeserializer(private val charset: Charset = Charsets.UTF_8) : Deserializable<String> {
    override fun deserialize(response: Response): String = String(response.data, charset)
}