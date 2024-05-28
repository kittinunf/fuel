package fuel.serialization

import com.github.kittinunf.result.Result
import fuel.HttpResponse
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json

public expect fun <T : Any> HttpResponse.toJson(
    json: Json = Json { allowStructuredMapKeys = true },
    deserializationStrategy: DeserializationStrategy<T>
): Result<T?, Throwable>