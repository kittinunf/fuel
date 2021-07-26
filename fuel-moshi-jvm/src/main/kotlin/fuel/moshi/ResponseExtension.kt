package fuel.moshi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import fuel.HttpResponse
import java.lang.reflect.Type

public val defaultMoshi: Moshi.Builder = Moshi.Builder()

public inline fun <reified T : Any> HttpResponse.toMoshi(): T? = toMoshi(T::class.java)

public fun <T : Any> HttpResponse.toMoshi(clazz: Class<T>): T? = defaultMoshi.build().adapter(clazz).fromJson(body)

public fun <T : Any> HttpResponse.toMoshi(type: Type): T? = defaultMoshi.build().adapter<T>(type).fromJson(body)

public fun <T : Any> HttpResponse.toMoshi(jsonAdapter: JsonAdapter<T>): T? = jsonAdapter.fromJson(body)
