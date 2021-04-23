package fuel.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json

public expect fun <T : Any> Any?.toJson(
    json: Json = Json { allowStructuredMapKeys = true },
    deserialization: DeserializationStrategy<T>
): T