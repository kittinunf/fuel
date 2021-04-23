package fuel.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import okhttp3.Response

public actual fun <T : Any> Any?.toJson(json: Json, deserialization: DeserializationStrategy<T>): T {
    require(this is Response)
    return json.decodeFromString(deserialization, body!!.string())
}