package fuel.moshi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okhttp3.Response
import java.lang.reflect.Type

val defaultMoshi = Moshi.Builder()

fun <T : Any> Response.toMoshi(clazz: Class<T>) =
    defaultMoshi.build().adapter(clazz).fromJson(body!!.source())

fun <T : Any> Response.toMoshi(type: Type) =
    defaultMoshi.build().adapter<T>(type).fromJson(body!!.source())

fun <T : Any> Response.toMoshi(jsonAdapter: JsonAdapter<T>) =
    jsonAdapter.fromJson(body!!.source())
