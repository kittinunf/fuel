package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.core.requests.CancellableRequest
import com.github.kittinunf.fuel.core.requests.RequestTaskCallbacks
import com.github.kittinunf.fuel.core.requests.suspendable
import com.github.kittinunf.fuel.core.requests.toTask
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.getOrElse
import com.github.kittinunf.result.map
import com.github.kittinunf.result.mapError
import java.io.InputStream
import java.io.Reader

interface Deserializable<out T : Any> {
    fun deserialize(response: Response): T
}

interface ResponseDeserializable<out T : Any> : Deserializable<T> {
    override fun deserialize(response: Response): T {
        response.body.toStream().use { stream ->
            return deserialize(stream)
                ?: deserialize(stream.reader())
                ?: response.let {
                    // Reassign the body here so it can be read once more.
                    val length = it.body.length
                    it.body = DefaultBody.from({ stream }, length?.let { l -> { l } })

                    deserialize(response.data)
                        ?: deserialize(String(response.data))
                        ?: throw FuelError(IllegalStateException(
                            "One of deserialize(ByteArray) or deserialize(InputStream) or deserialize(Reader) or " +
                                "deserialize(String) must be implemented"
                        ))
                }
        }
    }

    // One of these methods must be implemented
    fun deserialize(bytes: ByteArray): T? = null

    fun deserialize(inputStream: InputStream): T? = null

    fun deserialize(reader: Reader): T? = null

    fun deserialize(content: String): T? = null
}

fun <T : Any, U : Deserializable<T>> Request.response(deserializable: U, handler: ResponseResultHandler<T>): CancellableRequest =
    response(deserializable,
        { _, response, value -> handler(this@response, response, Result.Success(value)) },
        { _, response, error -> handler(this@response, response, Result.Failure(error)) }
    )

fun <T : Any, U : Deserializable<T>> Request.response(deserializable: U, handler: ResultHandler<T>): CancellableRequest =
    response(deserializable,
        { _, _, value -> handler(Result.Success(value)) },
        { _, _, error -> handler(Result.Failure(error)) }
    )

fun <T : Any, U : Deserializable<T>> Request.response(deserializable: U, handler: ResponseHandler<T>): CancellableRequest =
    response(deserializable,
        { request, response, value -> handler.success(request, response, value) },
        { request, response, error -> handler.failure(request, response, error) }
    )

fun <T : Any, U : Deserializable<T>> Request.response(deserializable: U, handler: Handler<T>): CancellableRequest =
    response(deserializable,
        { _, _, value -> handler.success(value) },
        { _, _, error -> handler.failure(error) }
    )

private fun <T : Any, U : Deserializable<T>> Request.response(
    deserializable: U,
    success: (Request, Response, T) -> Unit,
    failure: (Request, Response, FuelError) -> Unit
): CancellableRequest {
    val asyncRequest = RequestTaskCallbacks(
        request = this,
        onSuccess = { response ->
            val deliverable = Result.of<T, Exception> { deserializable.deserialize(response) }
            executionOptions.callback {
                deliverable.fold(
                    { success(this, response, it) },
                    { failure(this, response, FuelError.wrap(it)) }
                )
            }
        },
        onFailure = { error, response ->
            executionOptions.callback {
                failure(this, response, error)
            }
        }
    )

    return CancellableRequest(this, future = executionOptions.submit(asyncRequest))
}

fun <T : Any, U : Deserializable<T>> Request.response(deserializable: U): ResponseResultOf<T> {
    var response: Response? = null
    val result = Result.of<Response, Exception> { toTask().call() }
        .map {
            response = it
            deserializable.deserialize(it)
        }
        .mapError {
            if (it is FuelError) {
                response = it.response
                it
            } else {
                FuelError(it)
            }
        }

    return Triple(this, response ?: Response.error(), result)
}

/**
 * Await [T] or throws [FuelError]
 * @return [T] the [T]
 */
@Throws
suspend fun <T : Any, U : Deserializable<T>> Request.await(deserializable: U) : T {
    val response = suspendable().await()
    return deserializable.deserialize(response)
}

/**
 * Await [T] or [FuelError]
 * @return [ResponseOf<T>] the [Result] of [T]
 */
@Throws
suspend fun <T : Any, U : Deserializable<T>> Request.awaitResponse(deserializable: U) : ResponseOf<T> {
    val response = suspendable().await()
    return Triple(this, response, deserializable.deserialize(response))
}

/**
 * Await [T] or [FuelError]
 * @return [Result<T>] the [Result] of [T]
 */
suspend fun <T : Any, U : Deserializable<T>> Request.awaitResult(deserializable: U): Result<T, FuelError> {
    val initialResult = suspendable().awaitResult()
    return initialResult.map { deserializable.deserialize(it) }
        .mapError <T, Exception, FuelError> { FuelError.wrap(it) }
}

/**
 * Await [T] or [FuelError]
 * @return [ResponseResultOf<T>] the [ResponseResultOf] of [T]
 */
suspend fun <T : Any, U : Deserializable<T>> Request.awaitResponseResult(deserializable: U) : ResponseResultOf<T> {
    val initialResult = suspendable().awaitResult()
    return initialResult.map { deserializable.deserialize(it) }
        .mapError <T, Exception, FuelError> { FuelError.wrap(it) }
        .let { finalResult -> Triple(this, initialResult.getOrElse(Response.error()), finalResult) }
}
