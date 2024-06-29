package fuel.serialization

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.runCatching
import fuel.HttpResponse
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeFromSource

@OptIn(ExperimentalSerializationApi::class)
public fun <T : Any> HttpResponse.toJson(
    json: Json = Json,
    deserializationStrategy: DeserializationStrategy<T>
): Result<T?, Throwable> = runCatching {
    json.decodeFromSource(source = source, deserializer = deserializationStrategy)
}
