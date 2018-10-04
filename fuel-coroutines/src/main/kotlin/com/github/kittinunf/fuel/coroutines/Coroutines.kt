import com.github.kittinunf.fuel.core.Deserializable
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Request.Companion.byteArrayDeserializer
import com.github.kittinunf.fuel.core.Request.Companion.stringDeserializer
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.core.awaitResponse
import com.github.kittinunf.result.Result
import java.nio.charset.Charset
import kotlin.coroutines.CoroutineContext


private suspend fun <T : Any, U : Deserializable<T>> Request.await(
        deserializable: U, scope: CoroutineContext
): Triple<Request, Response, Result<T, FuelError>> =
        withContext(scope) {
            awaitResponse(deserializable)
        }


suspend fun Request.awaitByteArrayResponse(
        scope: CoroutineContext = CommonPool
): Triple<Request, Response, Result<ByteArray, FuelError>> =
        await(byteArrayDeserializer(), scope)

suspend fun Request.awaitStringResponse(
        charset: Charset = Charsets.UTF_8,
        scope: CoroutineContext = CommonPool
): Triple<Request, Response, Result<String, FuelError>> = await(stringDeserializer(charset), scope)

suspend fun <U : Any> Request.awaitObjectResponse(
        deserializable: ResponseDeserializable<U>,
        scope: CoroutineContext = CommonPool
): Triple<Request, Response, Result<U, FuelError>> = await(deserializable, scope)

/***
 *
 *  @param scope : This is the coroutine context you want the call to be made on, the defaut is CommonPool
 *
 *  @return ByteArray if no exceptions are thrown
 */
@Throws
suspend fun Request.awaitByteArray(
        scope: CoroutineContext = CommonPool
): ByteArray = await(byteArrayDeserializer(), scope).third.get()

/**
 *  @note errors thrown in deserialization will not be caught
 *
 *  @param charset this is defaults to UTF-8
 *  @param scope : This is the coroutine context you want the call to be made on, the defaut is CommonPool
 *
 *  @return ByteArray if no exceptions are thrown
 */
@Throws
suspend fun Request.awaitString(
        charset: Charset = Charsets.UTF_8,
        scope: CoroutineContext = CommonPool
): String = await(stringDeserializer(charset), scope).third.get()

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
suspend fun <U : Any> Request.awaitObject(
        deserializable: ResponseDeserializable<U>,
        scope: CoroutineContext = CommonPool
): U = await(deserializable, scope).third.get()

/***
 * Response functions all these return a Result
 *
 * @param scope : This is the coroutine context you want the call to be made on, the defaut is CommonPool
 *
 * @return Result<ByteArray,FuelError>
 */
suspend fun Request.awaitByteArrayResult(
        scope: CoroutineContext = CommonPool
): Result<ByteArray, FuelError> = awaitByteArrayResponse(scope).third

/**
 *
 * @param charset this is defaults to UTF-8
 * @param scope : This is the coroutine context you want the call to be made on, the defaut is CommonPool
 *
 * @return Result<String,FuelError>
 */
suspend fun Request.awaitStringResult(
        charset: Charset = Charsets.UTF_8,
        scope: CoroutineContext = CommonPool
): Result<String, FuelError> = awaitStringResponse(charset, scope).third


/**
 * This function catches both server errors and Deserialization Errors
 *
 * @param deserializable
 * @param scope : This is the coroutine context you want the call to be made on, the defaut is CommonPool
 *
 * @return Result object
 */
suspend fun <U : Any> Request.awaitObjectResult(
        deserializable: ResponseDeserializable<U>,
        scope: CoroutineContext = CommonPool
): Result<U, FuelError> = try {
    await(deserializable, scope).third
} catch (e: Exception) {
    val fuelError = when (e) {
        is FuelError -> e
        else -> FuelError(e)
    }
    Result.Failure(fuelError)
}

@Deprecated("please use 'awaitByteArray()'", ReplaceWith("awaitByteArray()", "deserializable"))
suspend fun Request.awaitResponseResult(): ByteArray = awaitByteArray()

@Deprecated("please use 'awaitObjectResult(deserializable)'", ReplaceWith("awaitObjectResult(deserializable)"))
suspend fun <U : Any> Request.awaitSafelyObjectResult(
        deserializable: ResponseDeserializable<U>
): Result<U, FuelError> = this.awaitObjectResult(deserializable)



