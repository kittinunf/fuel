package fuel.moshi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okhttp3.Response
import java.lang.reflect.Type

public val defaultMoshi: Moshi.Builder = Moshi.Builder()

public inline fun <reified T : Any> Any?.toMoshi(): T? = toMoshi(T::class.java)

public fun <T : Any> Any?.toMoshi(clazz: Class<T>): T? {
    require(this is Response)
    return defaultMoshi.build().adapter(clazz).fromJson(body!!.source())
}

public fun <T : Any> Any?.toMoshi(type: Type): T? {
    require(this is Response)
    return defaultMoshi.build().adapter<T>(type).fromJson(body!!.source())
}

public fun <T : Any> Any?.toMoshi(jsonAdapter: JsonAdapter<T>): T? {
    require(this is Response)
    return jsonAdapter.fromJson(body!!.source())
}

