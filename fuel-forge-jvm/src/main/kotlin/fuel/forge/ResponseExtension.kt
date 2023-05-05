package fuel.forge

import com.github.kittinunf.forge.Forge
import com.github.kittinunf.forge.core.DeserializedResult
import com.github.kittinunf.forge.core.JSON
import fuel.HttpResponse

public fun <T : Any> HttpResponse.toForge(deserializer: JSON.() -> DeserializedResult<T>): DeserializedResult<T> =
    Forge.modelFromJson(body.string(), deserializer)
