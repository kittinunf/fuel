package fuel.serialization

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.runCatching
import fuel.HttpResponse
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json

public fun <T : Any> HttpResponse.toJson(
    json: Json = Json { allowStructuredMapKeys = true },
    deserializationStrategy: DeserializationStrategy<T>
): Result<T?, Throwable> = runCatching {
    json.decodeFromString(deserializationStrategy, response?.json().toString())
}
