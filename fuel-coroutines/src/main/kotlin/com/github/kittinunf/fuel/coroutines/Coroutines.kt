package com.github.kittinunf.fuel.coroutines
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

/**
 * Await the [T] using a [scope], defaulting to [Dispatchers.IO]
 *
 * @throws FuelError if deserialization fails, if network fails, other internal exception is thrown
 *
 * @param deserializable [U] the instance that can turn the Response into a [T]
 * @param scope [CoroutineContext] the context to run within
 *
 * @return [T]
 */
@Throws
@JvmOverloads
suspend inline fun <T : Any, U : Deserializable<T>> Request.await(deserializable: U, scope: CoroutineContext = Dispatchers.IO): T =
    withContext(scope) { await(deserializable) }

/**
 * Await the [T] using a [scope], defaulting to [Dispatchers.IO], wrapped in [Result]
 *
 * @param deserializable [U] the instance that can turn the Response into a [T]
 * @param scope [CoroutineContext] the context to run within
 *
 * @return [Result] [T] or [FuelError]
 */
@JvmOverloads
suspend inline fun <T : Any, U : Deserializable<T>> Request.awaitResult(deserializable: U, scope: CoroutineContext = Dispatchers.IO): Result<T, FuelError> =
    withContext(scope) { awaitResult(deserializable) }

/**
 * Await the [T] using a [scope], defaulting to [Dispatchers.IO], including metadata
 *
 * @throws FuelError if deserialization fails, if network fails, other internal exception is thrown
 *
 * @param deserializable [U] the instance that can turn the Response into a [T]
 * @param scope [CoroutineContext] the context to run within
 *
 * @return [ResponseOf] [T]
 */
@Throws
@JvmOverloads
suspend inline fun <T : Any, U : Deserializable<T>> Request.awaitResponse(deserializable: U, scope: CoroutineContext = Dispatchers.IO): ResponseOf<T> =
    withContext(scope) { awaitResponse(deserializable) }

/**
 * Await the [T] using a [scope], defaulting to [Dispatchers.IO], wrapped in [Result], including metadata
 *
 * @param deserializable [U] the instance that can turn the Response into a [T]
 * @param scope [CoroutineContext] the context to run within
 *
 * @return [ResponseResultOf] [T] or [FuelError]
 */
@JvmOverloads
suspend inline fun <T : Any, U : Deserializable<T>> Request.awaitResponseResult(deserializable: U, scope: CoroutineContext = Dispatchers.IO): ResponseResultOf<T> =
    withContext(scope) { awaitResponseResult(deserializable) }

/***
 * Awaits the response as a [ByteArray] with [scope] as context
 *
 * @throws FuelError if deserialization fails, if network fails, other internal exception is thrown
 *
 * @param scope [CoroutineContext] the coroutine context you want the call to be made on, defaulting to [Dispatchers.IO]
 * @return [ByteArray] if no exceptions are thrown
 */
@Throws
@JvmOverloads
suspend inline fun Request.awaitByteArray(scope: CoroutineContext = Dispatchers.IO): ByteArray =
    await(ByteArrayDeserializer(), scope)

/***
 * Awaits the response as a [ByteArray] with [scope] as context, with metadata
 *
 * @throws FuelError if deserialization fails, if network fails, other internal exception is thrown
 *
 * @param scope [CoroutineContext] the coroutine context you want the call to be made on, defaulting to [Dispatchers.IO]
 * @return [ResponseOf] [ByteArray] if no exceptions are thrown
 */
@Throws
@JvmOverloads
suspend inline fun Request.awaitByteArrayResponse(scope: CoroutineContext = Dispatchers.IO): ResponseOf<ByteArray> =
    awaitResponse(ByteArrayDeserializer(), scope)

/***
 * Awaits the response as a [String] with [scope] as context
 *
 * @throws FuelError if deserialization fails, if network fails, other internal exception is thrown
 *
 * @param scope [CoroutineContext] the coroutine context you want the call to be made on, defaulting to [Dispatchers.IO]
 * @param charset [Charset] the charset to use for the [String], defaulting to [Charsets.UTF_8]
 *
 * @return [String] if no exceptions are thrown
 */
@Throws
@JvmOverloads
suspend inline fun Request.awaitString(charset: Charset = Charsets.UTF_8, scope: CoroutineContext = Dispatchers.IO): String =
    await(StringDeserializer(charset), scope)

/***
 * Awaits the response as a [String] with [scope] as context, with metadata
 *
 * @param scope [CoroutineContext] the coroutine context you want the call to be made on, defaulting to [Dispatchers.IO]
 * @param charset [Charset] the charset to use for the [String], defaulting to [Charsets.UTF_8]
 * @return [ResponseOf] [String] if no exceptions are thrown
 */
@Throws
@JvmOverloads
suspend inline fun Request.awaitStringResponse(charset: Charset = Charsets.UTF_8, scope: CoroutineContext = Dispatchers.IO): ResponseOf<String> =
    awaitResponse(StringDeserializer(charset), scope)

/***
 * Awaits the response as a [U] with [scope] as context
 *
 * @throws FuelError if deserialization fails, if network fails, other internal exception is thrown
 *
 * @param scope [CoroutineContext] the coroutine context you want the call to be made on, defaulting to [Dispatchers.IO]
 * @param deserializable [ResponseDeserializable] instance that can turn the response into a [U]
 *
 * @return [U] if no exceptions are thrown
 */
@Throws
@JvmOverloads
suspend inline fun <U : Any> Request.awaitObject(deserializable: ResponseDeserializable<U>, scope: CoroutineContext = Dispatchers.IO): U =
    await(deserializable, scope)

/***
 * Awaits the response as a [U] with [scope] as context, with metadata
 *
 * @throws FuelError if deserialization fails, if network fails, other internal exception is thrown
 *
 * @param scope [CoroutineContext] the coroutine context you want the call to be made on, defaulting to [Dispatchers.IO]
 * @param deserializable [ResponseDeserializable] instance that can turn the response into a [U]
 *
 * @return [ResponseOf] [U] if no exceptions are thrown
 */
@Throws
@JvmOverloads
suspend inline fun <U : Any> Request.awaitObjectResponse(deserializable: ResponseDeserializable<U>, scope: CoroutineContext = Dispatchers.IO): ResponseOf<U> =
    awaitResponse(deserializable, scope)

/***
 * Awaits the response as a [ByteArray] with [scope] as context
 *
 * @param scope [CoroutineContext] the coroutine context you want the call to be made on, defaulting to [Dispatchers.IO]
 * @return [Result] [ByteArray] or [FuelError]
 */
@JvmOverloads
suspend inline fun Request.awaitByteArrayResult(scope: CoroutineContext = Dispatchers.IO): Result<ByteArray, FuelError> =
    awaitResult(ByteArrayDeserializer(), scope)

/***
 * Awaits the response as a [ByteArray] with [scope] as context, with metadata
 *
 * @param scope [CoroutineContext] the coroutine context you want the call to be made on, defaulting to [Dispatchers.IO]
 * @return [ResponseResultOf] [ByteArray]
 */
@JvmOverloads
suspend inline fun Request.awaitByteArrayResponseResult(scope: CoroutineContext = Dispatchers.IO): ResponseResultOf<ByteArray> =
    awaitResponseResult(ByteArrayDeserializer(), scope)

/***
 * Awaits the response as a [String] with [scope] as context
 *
 * @param scope [CoroutineContext] the coroutine context you want the call to be made on, defaulting to [Dispatchers.IO]
 * @param charset [Charset] the charset to use for the [String], defaulting to [Charsets.UTF_8]
 *
 * @return [Result] [String] or [FuelError]
 */
@JvmOverloads
suspend inline fun Request.awaitStringResult(charset: Charset = Charsets.UTF_8, scope: CoroutineContext = Dispatchers.IO): Result<String, FuelError> =
    awaitResult(StringDeserializer(charset), scope)

/***
 * Awaits the response as a [String] with [scope] as context, with metadata
 *
 * @param scope [CoroutineContext] the coroutine context you want the call to be made on, defaulting to [Dispatchers.IO]
 * @param charset [Charset] the charset to use for the [String], defaulting to [Charsets.UTF_8]
 *
 * @return [ResponseResultOf] [String]
 */
@JvmOverloads
suspend inline fun Request.awaitStringResponseResult(charset: Charset = Charsets.UTF_8, scope: CoroutineContext = Dispatchers.IO): ResponseResultOf<String> =
    awaitResponseResult(StringDeserializer(charset), scope)

/***
 * Awaits the response as a [U] with [scope] as context
 *
 * @param scope [CoroutineContext] the coroutine context you want the call to be made on, defaulting to [Dispatchers.IO]
 * @param deserializable [ResponseDeserializable] instance that can turn the response into a [U]
 *
 * @return [Result] [U] or [FuelError]
 */
@JvmOverloads
suspend inline fun <U : Any> Request.awaitObjectResult(deserializable: ResponseDeserializable<U>, scope: CoroutineContext = Dispatchers.IO): Result<U, FuelError> =
    awaitResult(deserializable, scope)

/***
 * Awaits the response as a [U] with [scope] as context, with metadata
 *
 * @param scope [CoroutineContext] the coroutine context you want the call to be made on, defaulting to [Dispatchers.IO]
 * @param deserializable [ResponseDeserializable] instance that can turn the response into a [U]
 *
 * @return [ResponseResultOf] [U]
 */
@JvmOverloads
suspend inline fun <U : Any> Request.awaitObjectResponseResult(deserializable: ResponseDeserializable<U>, scope: CoroutineContext = Dispatchers.IO): ResponseResultOf<U> =
    awaitResponseResult(deserializable, scope)
