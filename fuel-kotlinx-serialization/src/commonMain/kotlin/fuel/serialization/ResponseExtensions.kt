package fuel.serialization

import fuel.HttpResponse
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json

public fun <T : Any> HttpResponse.toJson(
    json: Json = Json { allowStructuredMapKeys = true },
    deserializationStrategy: DeserializationStrategy<T>
) : T = json.decodeFromString(deserializationStrategy, body)
