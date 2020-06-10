package fuel.forge

import com.github.kittinunf.forge.Forge
import com.github.kittinunf.forge.core.DeserializedResult
import com.github.kittinunf.forge.core.JSON
import okhttp3.Response

fun <T : Any> Response.toForge(deserializer: JSON.() -> DeserializedResult<T>) =
    Forge.modelFromJson(body!!.string(), deserializer)
