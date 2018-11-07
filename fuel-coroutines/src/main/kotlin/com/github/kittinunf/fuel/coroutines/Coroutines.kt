
import com.github.kittinunf.fuel.core.Deserializable
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.core.ResponseOf
import com.github.kittinunf.fuel.core.ResponseResultOf
import com.github.kittinunf.fuel.core.await
import com.github.kittinunf.fuel.core.awaitResponse
import com.github.kittinunf.fuel.core.awaitResponseResult
import com.github.kittinunf.fuel.core.awaitResult
import com.github.kittinunf.fuel.core.deserializers.ByteArrayDeserializer
import com.github.kittinunf.fuel.core.deserializers.StringDeserializer
import com.github.kittinunf.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.charset.Charset
import kotlin.coroutines.CoroutineContext

suspend inline fun <T : Any, U : Deserializable<T>> Request.await(deserializable: U, scope: CoroutineContext = Dispatchers.Default): T =
    withContext(scope) { await(deserializable) }

suspend inline fun <T : Any, U : Deserializable<T>> Request.awaitResult(deserializable: U, scope: CoroutineContext = Dispatchers.Default): Result<T, FuelError> =
    withContext(scope) { awaitResult(deserializable) }

suspend inline fun <T : Any, U : Deserializable<T>> Request.awaitResponse(deserializable: U, scope: CoroutineContext = Dispatchers.Default): ResponseOf<T> =
    withContext(scope) { awaitResponse(deserializable) }

suspend inline fun <T : Any, U : Deserializable<T>> Request.awaitResponseResult(deserializable: U, scope: CoroutineContext = Dispatchers.Default): ResponseResultOf<T> =
    withContext(scope) { awaitResponseResult(deserializable) }

/***
 *
 *  @param scope : This is the coroutine context you want the call to be made on, the defaut is CommonPool
 *
 *  @return ByteArray if no exceptions are thrown
 */
@Throws
suspend inline fun Request.awaitByteArray(scope: CoroutineContext = Dispatchers.Default): ByteArray =
    await(ByteArrayDeserializer(), scope)

@Throws
suspend inline fun Request.awaitByteArrayResponse(scope: CoroutineContext = Dispatchers.Default): ResponseOf<ByteArray> =
    awaitResponse(ByteArrayDeserializer(), scope)

/**
 *  @note errors thrown in deserialization will not be caught
 *
 *  @param charset this is defaults to UTF-8
 *  @param scope : This is the coroutine context you want the call to be made on, the defaut is CommonPool
 *
 *  @return ByteArray if no exceptions are thrown
 */
@Throws
suspend inline fun Request.awaitString(charset: Charset = Charsets.UTF_8, scope: CoroutineContext = Dispatchers.Default): String =
    await(StringDeserializer(charset), scope)

@Throws
suspend inline fun Request.awaitStringResponse(charset: Charset = Charsets.UTF_8, scope: CoroutineContext = Dispatchers.Default): ResponseOf<String> =
    awaitResponse(StringDeserializer(charset), scope)

/**
 * @note This function will throw the an exception if an error is thrown either at the HTTP level
 * or during deserialization
 *
 * @param deserializable
 * @param scope : This is the coroutine context you want the call to be made on, the defaut is CommonPool
 *
 * @return Result object
 */
@Throws
suspend fun <U : Any> Request.awaitObject(deserializable: ResponseDeserializable<U>, scope: CoroutineContext = Dispatchers.Default): U =
    await(deserializable, scope)

@Throws
suspend fun <U : Any> Request.awaitObjectResponse(deserializable: ResponseDeserializable<U>, scope: CoroutineContext = Dispatchers.Default): ResponseOf<U> =
    awaitResponse(deserializable, scope)

/***
 * Response functions all these return a Result
 *
 * @param scope : This is the coroutine context you want the call to be made on, the defaut is CommonPool
 *
 * @return Result<ByteArray,FuelError>
 */
suspend inline fun Request.awaitByteArrayResult(scope: CoroutineContext = Dispatchers.Default): Result<ByteArray, FuelError> =
    awaitResult(ByteArrayDeserializer(), scope)

suspend inline fun Request.awaitByteArrayResponseResult(scope: CoroutineContext = Dispatchers.Default): ResponseResultOf<ByteArray> =
    awaitResponseResult(ByteArrayDeserializer(), scope)

/**
 *
 * @param charset this is defaults to UTF-8
 * @param scope : This is the coroutine context you want the call to be made on, the defaut is CommonPool
 *
 * @return Result<String,FuelError>
 */
suspend inline fun Request.awaitStringResult(charset: Charset = Charsets.UTF_8, scope: CoroutineContext = Dispatchers.Default): Result<String, FuelError> =
    awaitResult(StringDeserializer(charset), scope)

suspend inline fun Request.awaitStringResponseResult(charset: Charset = Charsets.UTF_8, scope: CoroutineContext = Dispatchers.Default): ResponseResultOf<String> =
    awaitResponseResult(StringDeserializer(charset), scope)

/**
 * This function catches both server errors and Deserialization Errors
 *
 * @param deserializable
 * @param scope : This is the coroutine context you want the call to be made on, the defaut is CommonPool
 *
 * @return Result object
 */
suspend inline fun <U : Any> Request.awaitObjectResult(deserializable: ResponseDeserializable<U>, scope: CoroutineContext = Dispatchers.Default): Result<U, FuelError> =
    awaitResult(deserializable, scope)

suspend inline fun <U : Any> Request.awaitObjectResponseResult(deserializable: ResponseDeserializable<U>, scope: CoroutineContext = Dispatchers.Default): ResponseResultOf<U> =
    awaitResponseResult(deserializable, scope)
