package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.core.executors.AsyncRequestExecutor
import com.github.kittinunf.fuel.core.executors.CancellableRequest
import com.github.kittinunf.fuel.core.executors.SuspendingRequestExecutor
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import com.github.kittinunf.result.mapError
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringReader

typealias HandlerWithResult<T> = (Request, Response, Result<T, FuelError>) -> Unit

interface Deserializable<out T : Any> {
    fun deserialize(response: Response): T
}

interface ResponseDeserializable<out T : Any> : Deserializable<T> {
    override fun deserialize(response: Response): T {
        try {
            return deserialize(response.dataStream) ?: deserialize(response.data) ?: deserialize(String(response.data))
            ?: throw IllegalStateException(
                "One of deserialize(ByteArray), deserialize(InputStream), deserialize(Reader) or deserialize(String) " +
                    "must be implemented"
            )
        } finally {
            // The stream might already be closed
            try { response.dataStream.close() } catch (ignore: IOException) {}
        }
    }

    fun deserialize(content: String): T? = deserialize(StringReader(content))
    fun deserialize(bytes: ByteArray): T? = deserialize(ByteArrayInputStream(bytes))
    fun deserialize(inputStream: InputStream): T? = deserialize(InputStreamReader(inputStream))
    fun deserialize(reader: Reader): T? = null
}

fun <T : Any, U : Deserializable<T>> Request.response(deserializable: U, handler: HandlerWithResult<T>): CancellableRequest {
    return response(deserializable, { request, response, value ->
        handler(request, response, Result.Success(value))
    }, { request, response, error ->
        handler(request, response, Result.Failure(error))
    })
}

fun <T : Any, U : Deserializable<T>> Request.response(deserializable: U, handler: Handler<T>): CancellableRequest {
    return response(deserializable, { request, response, value ->
        handler.success(request, response, value)
    }, { request, response, error ->
        handler.failure(request, response, error)
    })
}

private fun <T : Any, U : Deserializable<T>> Request.response(
    deserializable: U,
    success: (Request, Response, T) -> Unit,
    failure: (Request, Response, FuelError) -> Unit
): CancellableRequest {

    val asyncRequest = AsyncRequestExecutor(executor)

    asyncRequest.successCallback = { response ->
        val deliverable = Result.of<T, Exception> { deserializable.deserialize(response) }
        asyncRequest.callback {
            deliverable.fold({
                success(this, response, it)
            }, {
                failure(this, response, FuelError(it))
            })
        }
    }

    asyncRequest.failureCallback = { error, response ->
        asyncRequest.callback {
            failure(this, response, error)
        }
    }

    return CancellableRequest.from(this, asyncRequest.submit())
}

fun <T : Any, U : Deserializable<T>> Request.response(deserializable: U): Triple<Request, Response, Result<T, FuelError>> {
    var response: Response? = null
    val result = Result.of<Response, Exception> { executor.call() }
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

suspend fun <T : Any, U : Deserializable<T>> Request.awaitResponse(deserializable: U): Triple<Request, Response, Result<T, FuelError>> {
    val r = SuspendingRequestExecutor(executor).awaitResult()
    val res =
        r.map {
            deserializable.deserialize(it)
        }.mapError {
            it as? FuelError ?: FuelError(it)
        }

    return Triple(this, r.component1() ?: Response.error(), res)
}