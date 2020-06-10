package fuel.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.Response

fun <T : Any> Response.toJson(
    json: Json = Json(JsonConfiguration.Stable),
    deserialization: DeserializationStrategy<T>
) = json.parse(deserialization, body!!.string())
