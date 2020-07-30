package fuel.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import okhttp3.Response

fun <T : Any> Response.toJson(
    json: Json = Json { allowStructuredMapKeys = true },
    deserialization: DeserializationStrategy<T>
) = json.decodeFromString(deserialization, body!!.string())
