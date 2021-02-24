package fuel.moshi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okhttp3.Response
import java.lang.reflect.Type

public val defaultMoshi: Moshi.Builder = Moshi.Builder()

public inline fun <reified T : Any> Response.toMoshi(): T? = toMoshi(T::class.java)

public fun <T : Any> Response.toMoshi(clazz: Class<T>): T? =
    defaultMoshi.build().adapter(clazz).fromJson(body!!.source())

public fun <T : Any> Response.toMoshi(type: Type): T? =
    defaultMoshi.build().adapter<T>(type).fromJson(body!!.source())

public fun <T : Any> Response.toMoshi(jsonAdapter: JsonAdapter<T>): T? =
    jsonAdapter.fromJson(body!!.source())
