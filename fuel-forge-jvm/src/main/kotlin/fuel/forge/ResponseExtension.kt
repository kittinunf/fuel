package fuel.forge

import com.github.kittinunf.forge.Forge
import com.github.kittinunf.forge.core.DeserializedResult
import com.github.kittinunf.forge.core.JSON
import okhttp3.Response

public fun <T : Any> Any?.toForge(deserializer: JSON.() -> DeserializedResult<T>): DeserializedResult<T> {
    require(this is Response)
    return Forge.modelFromJson(body!!.string(), deserializer)
}
