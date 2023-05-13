package fuel.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.runCatching
import fuel.HttpResponse

public inline fun <reified T : Any> HttpResponse.toJackson(
    mapper: ObjectMapper = jacksonObjectMapper()
): Result<T?, Throwable> =
    runCatching {
        mapper.readValue(body.string())
    }
