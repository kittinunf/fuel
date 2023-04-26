package fuel.moshi

import com.github.kittinunf.result.Result
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import fuel.HttpResponse
import java.io.IOException
import java.lang.reflect.Type

public val defaultMoshi: Moshi.Builder = Moshi.Builder()

public inline fun <reified T : Any> HttpResponse.toMoshi(): Result<T?, IOException> = toMoshi(T::class.java)

public fun <T : Any> HttpResponse.toMoshi(clazz: Class<T>): Result<T?, IOException> = try {
    Result.success(defaultMoshi.build().adapter(clazz).fromJson(source))
} catch (ioe: IOException) {
    Result.failure(ioe)
}

public fun <T : Any> HttpResponse.toMoshi(type: Type): Result<T?, IOException> = try {
    Result.success(defaultMoshi.build().adapter<T>(type).fromJson(source))
} catch (ioe: IOException) {
    Result.failure(ioe)
}

public fun <T : Any> HttpResponse.toMoshi(jsonAdapter: JsonAdapter<T>): Result<T?, IOException> = try {
    Result.success(jsonAdapter.fromJson(source))
} catch (ioe: IOException) {
    Result.failure(ioe)
}
