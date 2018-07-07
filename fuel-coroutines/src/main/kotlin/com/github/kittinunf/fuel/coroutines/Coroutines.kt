import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.fuel.core.Request.Companion.byteArrayDeserializer
import com.github.kittinunf.fuel.core.Request.Companion.stringDeserializer
import com.github.kittinunf.result.Result
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import java.nio.charset.Charset

private suspend fun <T : Any, U : Deserializable<T>> Request.await(
        deserializable: U
): Triple<Request, Response, Result<T, FuelError>> =
        suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { cancel() }
            response(deserializable) { request: Request, response: Response, result: Result<T, FuelError> ->
                result.fold({
                    continuation.resume(Triple(request, response, result))
                }, {
                    continuation.resumeWithException(it.exception)
                })
            }
        }

private suspend fun <T : Any, U : Deserializable<T>> Request.awaitResult(
        deserializable: U
): Triple<Request, Response, Result<T, FuelError>> =
        suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { cancel() }
            response(deserializable) { request: Request, response: Response, result: Result<T, FuelError> ->
                continuation.resume(Triple(request, response, result))
            }
        }

/**
 *
 * Response functions all these return
 *
 * Triple<Request, Response, Result<T, FuelError>>
 *
 */
suspend fun Request.awaitResponse(): Triple<Request, Response, Result<ByteArray, FuelError>> =
        awaitResult(byteArrayDeserializer())

suspend fun Request.awaitStringResponse(
        charset: Charset = Charsets.UTF_8
): Triple<Request, Response, Result<String, FuelError>> = awaitResult(stringDeserializer(charset))


/**
 * @note errors throws in deserialization will not be caught and returned as part of the fuel error
 *
 */
suspend fun <U : Any> Request.awaitObjectResponse(
        deserializable: ResponseDeserializable<U>
): Triple<Request, Response, Result<U, FuelError>> = awaitResult(deserializable)


/***
 *
 * Response functions all these return a Type
 *
 *  @return ByteArray if no exceptions are thrown
 */
@Throws
suspend fun Request.awaitForByteArray(): ByteArray = await(byteArrayDeserializer()).third.get()

/**
 *  @note errors thrown in deserialization will not be caught
 *
 *  @return ByteArray if no exceptions are thrown
 */
@Throws
suspend fun Request.awaitForString(charset: Charset = Charsets.UTF_8): String = await(stringDeserializer(charset)).third.get()

/**
 * This function will throw the an exception if an error is thrown either at the HTTP level
 * or during deserialization
 *
 * @param deserializable
 *
 * @return Result object
 */
@Throws
suspend fun <U : Any> Request.awaitForObject(deserializable: ResponseDeserializable<U>): U = await(deserializable).third.get()

/***
 *
 * Response functions all these return a Result
 *
 * @return Result<ByteArray,FuelError>
 */
suspend fun Request.awaitForByteArrayResult(): Result<ByteArray, FuelError> = awaitResponse().third

/**
 *
 * @param charset this is defaults to UTF-8
 *
 * @return Result<String,FuelError>
 */
suspend fun Request.awaitForStringResult(
        charset: Charset = Charsets.UTF_8
): Result<String, FuelError> = awaitStringResponse(charset).third

/**
 * This function catches both server errors and Deserialization Errors
 *
 * @param deserializable
 *
 * @return Result object
 */
suspend fun <U : Any> Request.awaitForObjectResult(
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

@Deprecated("please use 'awaitForByteArray()'", ReplaceWith("awaitForByteArray()", "deserializable"))
suspend fun Request.awaitResponseResult(): ByteArray = awaitForByteArray()

@Deprecated("please use 'awaitForString()'", ReplaceWith("awaitForString(charset)", "charset"))
suspend fun Request.awaitStringResult(
        charset: Charset = Charsets.UTF_8
): String = awaitForString(charset)

@Deprecated("please use 'awaitForObject(deserializable)'", ReplaceWith("awaitForObject(deserializable)"))
suspend fun <U : Any> Request.awaitObjectResult(
        deserializable: ResponseDeserializable<U>
): U = awaitForObject(deserializable)

@Deprecated("please use 'awaitForObjectResult(deserializable)'", ReplaceWith("awaitForObjectResult(deserializable)"))
suspend fun <U : Any> Request.awaitSafelyObjectResult(
        deserializable: ResponseDeserializable<U>
): Result<U, FuelError> = awaitForObjectResult(deserializable)

@Deprecated("please use 'awaitStringResponse()'", ReplaceWith("awaitStringResponse()"))
suspend fun Request.awaitString(
        charset: Charset = Charsets.UTF_8
): Triple<Request, Response, Result<String, FuelError>> = awaitStringResponse(charset)

@Deprecated("please use 'awaitObjectResponse()'", ReplaceWith("awaitObjectResponse(deserializable)"))
suspend fun <U : Any> Request.awaitObject(
        deserializable: ResponseDeserializable<U>
): Triple<Request, Response, Result<U, FuelError>> = awaitObjectResponse(deserializable)