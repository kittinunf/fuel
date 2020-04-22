package fuel.moshi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import java.lang.reflect.Type
import okhttp3.Response

val defaultMoshi = Moshi.Builder()

fun <T : Any> Response.toMoshi(clazz: Class<T>) = body?.use {
    defaultMoshi.build().adapter(clazz).fromJson(it.source())
}

fun <T : Any> Response.toMoshi(type: Type) = body?.use {
    defaultMoshi.build().adapter<T>(type).fromJson(it.source())
}

fun <T : Any> Response.toMoshi(jsonAdapter: JsonAdapter<T>) = body?.use {
    jsonAdapter.fromJson(it.source())
}
