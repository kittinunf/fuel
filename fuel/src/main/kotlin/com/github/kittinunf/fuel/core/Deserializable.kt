package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.requests.CancellableRequest
import com.github.kittinunf.fuel.core.requests.DefaultBody
import com.github.kittinunf.fuel.core.requests.RequestTaskCallbacks
import com.github.kittinunf.fuel.core.requests.suspendable
import com.github.kittinunf.fuel.core.requests.toTask
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.getOrElse
import com.github.kittinunf.result.map
import com.github.kittinunf.result.mapError
import java.io.InputStream
import java.io.Reader

/**
 * Generic interface for [Response] deserialization.
 *
 * @note you are responsible of using the [Response] [Body] [InputStream] and closing it when you're done. Failing to do
 *   so can result in hanging connections if used in conjunction with [com.github.kittinunf.fuel.toolbox.HttpClient].
 *
 * @see ResponseDeserializable
 */
interface Deserializable<out T : Any> {

    /**
     * Deserialize [response] into [T]
     *
     * @param response [Response] the incoming response
     * @return [T] the instance of [T]
     */
    fun deserialize(response: Response): T
}

interface ResponseDeserializable<out T : Any> : Deserializable<T> {
    override fun deserialize(response: Response): T {
        response.body.toStream().use { stream ->
            return deserialize(stream)
                ?: deserialize(stream.reader())
                ?: reserialize(response, stream).let {
                    deserialize(response.data)
                        ?: deserialize(String(response.data))
                        ?: throw FuelError.wrap(IllegalStateException(
                            "One of deserialize(ByteArray) or deserialize(InputStream) or deserialize(Reader) or " +
                                "deserialize(String) must be implemented"
                        ))
                }
        }
    }

    private fun reserialize(response: Response, stream: InputStream): Response {
        val length = response.body.length
        response.body = DefaultBody.from({ stream }, length?.let { l -> { l } })
        return response
    }

    /**
     * Deserialize into [T] from an [InputStream]
     *
     * @param inputStream [InputStream] source bytes
     * @return [T] deserialized instance of [T] or null when not applied
     */
    fun deserialize(inputStream: InputStream): T? = null

    /**
     * Deserialize into [T] from a [Reader]
     *
     * @param reader [Reader] source bytes
     * @return [T] deserialized instance of [T] or null when not applied
     */
    fun deserialize(reader: Reader): T? = null

    /**
     * Deserialize into [T] from a [ByteArray]
     *
     * @note it is more efficient to implement the [InputStream] variant.
     *
     * @param bytes [ByteArray] source bytes
     * @return [T] deserialized instance of [T] or null when not applied
     */
    fun deserialize(bytes: ByteArray): T? = null

    /**
     * Deserialize into [T] from a [String]
     *
     * @note it is more efficient to implement the [Reader] variant.
     *
     * @param content [String] source bytes
     * @return [T] deserialized instance of [T] or null when not applied
     */
    fun deserialize(content: String): T? = null
}

/**
 * Deserialize the [Response] to the [this] into a [T] using [U]
 *
 * @see ResponseResultHandler
 *
 * @param deserializable [U] the instance that performs deserialization
 * @param handler [ResponseResultHandler<T>] handler that has a [Result]
 * @return [CancellableRequest] the request that can be cancelled
 */
fun <T : Any, U : Deserializable<T>> Request.response(deserializable: U, handler: ResponseResultHandler<T>): CancellableRequest =
    response(deserializable,
        { request, response, value -> handler(request, response, Result.Success(value)) },
        { request, response, error -> handler(request, response, Result.Failure(error)) }
    )

/**
 * Deserialize the [Response] to the [this] into a [T] using [U]
 *
 * @see ResultHandler
 *
 * @param deserializable [U] the instance that performs deserialization
 * @param handler [ResultHandler<T>] handler that has a [Result]
 * @return [CancellableRequest] the request that can be cancelled
 */
fun <T : Any, U : Deserializable<T>> Request.response(deserializable: U, handler: ResultHandler<T>): CancellableRequest =
    response(deserializable,
        { _, _, value -> handler(Result.Success(value)) },
        { _, _, error -> handler(Result.Failure(error)) }
    )

/**
 * Deserialize the [Response] to the [this] into a [T] using [U]
 *
 * @see ResponseHandler
 *
 * @param deserializable [U] the instance that performs deserialization
 * @param handler [ResponseHandler<T>] handler that has dedicated paths for success and failure
 * @return [CancellableRequest] the request that can be cancelled
 */
fun <T : Any, U : Deserializable<T>> Request.response(deserializable: U, handler: ResponseHandler<T>): CancellableRequest =
    response(deserializable,
        { request, response, value -> handler.success(request, response, value) },
        { request, response, error -> handler.failure(request, response, error) }
    )

/**
 * Deserialize the [Response] to the [this] into a [T] using [U]
 *
 * @see Handler
 *
 * @param deserializable [U] the instance that performs deserialization
 * @param handler [Handler<T>] handler that has dedicated paths for success and failure
 * @return [CancellableRequest] the request that can be cancelled
 */
fun <T : Any, U : Deserializable<T>> Request.response(deserializable: U, handler: Handler<T>): CancellableRequest =
    response(deserializable,
        { _, _, value -> handler.success(value) },
        { _, _, error -> handler.failure(error) }
    )

/**
 * Deserialize the [Response] to the [this] into a [T] using [U]
 *
 * @note not async, use the variations with a handler instead.
 *
 * @throws Exception if there is an internal library error, not related to Network or Deserialization
 *
 * @param deserializable [U] the instance that performs deserialization
 * @return [ResponseResultOf<T>] the response result of
 */
fun <T : Any, U : Deserializable<T>> Request.response(deserializable: U): ResponseResultOf<T> {
    // First execute the network request and catch any issues
    val rawResponse = runCatching { toTask().call() }
        .onFailure { error ->
            FuelError.wrap(error, Response.error(url)).also {
                return Triple(this, it.response, Result.error(it))
            }
        }
        .getOrThrow()

    // By this time it should have a response, but deserialization might fail
    return runCatching { Triple(this, rawResponse, Result.Success<T, FuelError>(deserializable.deserialize(rawResponse))) }
        .recover { error -> Triple(this, rawResponse, Result.Failure<T, FuelError>(FuelError.wrap(error, rawResponse))) }
        .getOrThrow()
}

private fun <T : Any, U : Deserializable<T>> Request.response(
    deserializable: U,
    success: (Request, Response, T) -> Unit,
    failure: (Request, Response, FuelError) -> Unit
): CancellableRequest {
    val asyncRequest = RequestTaskCallbacks(
        request = this,
        onSuccess = { response ->
            // The network succeeded but deserialization might fail
            val deliverable = Result.of<T, Exception> { deserializable.deserialize(response) }
            executionOptions.callback {
                deliverable.fold(
                    { success(this, response, it) },
                    { failure(this, response, FuelError.wrap(it, response).also { error ->
                        Fuel.trace { "[Deserializable] unfold failure: \n\r$error" } })
                    }
                )
            }
        },
        onFailure = { error, response ->
            executionOptions.callback {
                failure(this, response, error.also { error ->
                    Fuel.trace { "[Deserializable] callback failure: \n\r$error" }
                })
            }
        }
    )

    return CancellableRequest.enableFor(this, future = executionOptions.submit(asyncRequest))
}

/**
 * Await [T] or throws [FuelError]
 * @return [T] the [T]
 */
@Throws(FuelError::class)
suspend fun <T : Any, U : Deserializable<T>> Request.await(deserializable: U): T {
    val response = suspendable().await()
    return runCatching { deserializable.deserialize(response) }
        .onFailure { throw FuelError.wrap(it, response) }
        .getOrThrow()
}

/**
 * Await [T] or [FuelError]
 * @return [ResponseOf<T>] the [Result] of [T]
 */
@Throws(FuelError::class)
suspend fun <T : Any, U : Deserializable<T>> Request.awaitResponse(deserializable: U): ResponseOf<T> {
    val response = suspendable().await()
    return runCatching { Triple(this, response, deserializable.deserialize(response)) }
        .onFailure { throw FuelError.wrap(it, response) }
        .getOrThrow()
}

/**
 * Await [T] or [FuelError]
 * @return [Result<T>] the [Result] of [T]
 */
suspend fun <T : Any, U : Deserializable<T>> Request.awaitResult(deserializable: U): Result<T, FuelError> {
    val initialResult = suspendable().awaitResult()
    return serializeFor(initialResult, deserializable).map { (_, t) -> t }
}

/**
 * Await [T] or [FuelError]
 * @return [ResponseResultOf<T>] the [ResponseResultOf] of [T]
 */
suspend fun <T : Any, U : Deserializable<T>> Request.awaitResponseResult(deserializable: U): ResponseResultOf<T> {
    val initialResult = suspendable().awaitResult()
    return serializeFor(initialResult, deserializable).let {
            Triple(this,
                it.fold({ (response, _) -> response }, { error -> error.response }),
                it.map { (_, t) -> t }
            )
        }
}

private fun <T : Any, U : Deserializable<T>> serializeFor(result: Result<Response, FuelError>, deserializable: U) =
    result.map { (it to deserializable.deserialize(it)) }
        .mapError <Pair<Response, T>, Exception, FuelError> { FuelError.wrap(it, result.getOrElse(Response.error())) }
