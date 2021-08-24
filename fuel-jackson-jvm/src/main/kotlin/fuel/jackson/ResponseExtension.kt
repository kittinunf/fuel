package fuel.jackson

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.doTry
import fuel.HttpResponse

public val defaultMapper: ObjectMapper = ObjectMapper().registerKotlinModule()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

public inline fun <reified T : Any> HttpResponse.toJackson(mapper: ObjectMapper = defaultMapper): Result<T, Throwable> =
    doTry(work = {
        Result.success(mapper.readValue(body))
    }, errorHandler = {
        Result.failure(it)
    })
