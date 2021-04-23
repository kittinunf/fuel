package fuel.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json

public actual fun <T : Any> Any?.toJson(json: Json, deserialization: DeserializationStrategy<T>): T {
    require(this is String)
    return json.decodeFromString(deserialization, this)
}
