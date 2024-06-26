package fuel.serialization

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.runCatching
import fuel.HttpResponse
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json

public actual fun <T : Any> HttpResponse.toJson(
    json: Json,
    deserializationStrategy: DeserializationStrategy<T>
): Result<T?, Throwable> = runCatching {
    body?.let { json.decodeFromString(deserializationStrategy, it) }
}
