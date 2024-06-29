package fuel.forge

import com.github.kittinunf.forge.Forge
import com.github.kittinunf.forge.core.DeserializedResult
import com.github.kittinunf.forge.core.JSON
import fuel.HttpResponse
import kotlinx.io.readString

public fun <T : Any> HttpResponse.toForge(deserializer: JSON.() -> DeserializedResult<T>): DeserializedResult<T> =
    Forge.modelFromJson(source.readString(), deserializer)
