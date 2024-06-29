package fuel.moshi

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.runCatching
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import fuel.HttpResponse
import kotlinx.io.readByteArray
import okio.Buffer
import java.lang.reflect.Type

public val defaultMoshi: Moshi.Builder = Moshi.Builder()

public inline fun <reified T : Any> HttpResponse.toMoshi(): Result<T?, Throwable> = toMoshi(T::class.java)

public fun <T : Any> HttpResponse.toMoshi(clazz: Class<T>): Result<T?, Throwable> = toMoshi(defaultMoshi.build().adapter(clazz))

public fun <T : Any> HttpResponse.toMoshi(type: Type): Result<T?, Throwable> = toMoshi(defaultMoshi.build().adapter(type))

public fun <T : Any> HttpResponse.toMoshi(jsonAdapter: JsonAdapter<T>): Result<T?, Throwable> {
    val buffer = Buffer().apply { write(source.readByteArray()) }
    return runCatching {
        jsonAdapter.fromJson(buffer)
    }
}
