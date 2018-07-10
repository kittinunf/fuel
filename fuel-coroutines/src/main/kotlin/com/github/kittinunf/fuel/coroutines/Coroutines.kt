
import com.github.kittinunf.fuel.core.Deserializable
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Request.Companion.byteArrayDeserializer
import com.github.kittinunf.fuel.core.Request.Companion.stringDeserializer
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.core.response
import com.github.kittinunf.result.Result
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import java.nio.charset.Charset

private suspend fun <T : Any, U : Deserializable<T>> Request.await(
        deserializable: U
): Triple<Request, Response, Result<T, FuelError>> =
        suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { cancel() }
            continuation.resume(response(deserializable))
        }


suspend fun Request.awaitByteArrayResponse(): Triple<Request, Response, Result<ByteArray, FuelError>> =
        await(byteArrayDeserializer())

suspend fun Request.awaitStringResponse(
        charset: Charset = Charsets.UTF_8
): Triple<Request, Response, Result<String, FuelError>> = await(stringDeserializer(charset))

suspend fun <U : Any> Request.awaitObjectResponse(
        deserializable: ResponseDeserializable<U>
): Triple<Request, Response, Result<U, FuelError>> = await(deserializable)

/***
 *
 * Response functions all these return a Type
 *
 *  @return ByteArray if no exceptions are thrown
 */
@Throws
suspend fun Request.awaitByteArray(): ByteArray = await(byteArrayDeserializer()).third.get()

/**
 *  @note errors thrown in deserialization will not be caught
 *
 *  @return ByteArray if no exceptions are thrown
 */
@Throws
suspend fun Request.awaitString(
        charset: Charset = Charsets.UTF_8
): String = await(stringDeserializer(charset)).third.get()

/**
 * @note This function will throw the an exception if an error is thrown either at the HTTP level
 * or during deserialization
 *
 * @param deserializable
 *
 * @return Result object
 */
@Throws
suspend fun <U : Any> Request.awaitObject(
        deserializable: ResponseDeserializable<U>
): U = await(deserializable).third.get()

/***
 *
 * Response functions all these return a Result
 *
 * @return Result<ByteArray,FuelError>
 */
suspend fun Request.awaitByteArrayResult(): Result<ByteArray, FuelError> = awaitByteArrayResponse().third

/**
 *
 * @param charset this is defaults to UTF-8
 *
 * @return Result<String,FuelError>
 */
suspend fun Request.awaitStringResult(
        charset: Charset = Charsets.UTF_8
): Result<String, FuelError> = awaitStringResponse(charset).third


/**
 * This function catches both server errors and Deserialization Errors
 *
 * @param deserializable
 *
 * @return Result object
 */
suspend fun <U : Any> Request.awaitObjectResult(
        deserializable: ResponseDeserializable<U>
): Result<U, FuelError> = try {
    await(deserializable).third
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



