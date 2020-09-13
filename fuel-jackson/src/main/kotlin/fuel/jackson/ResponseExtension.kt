package fuel.jackson

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import okhttp3.Response

public val defaultMapper: ObjectMapper = ObjectMapper().registerKotlinModule()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

public inline fun <reified T: Any> Response.toJackson(mapper: ObjectMapper = defaultMapper) : T =
    mapper.readValue(body!!.string())