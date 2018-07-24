package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.core.requests.AsyncTaskRequest
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import com.github.kittinunf.result.mapError
import java.io.InputStream
import java.io.Reader

interface Deserializable<out T : Any> {
    fun deserialize(response: Response): T
}

interface ResponseDeserializable<out T : Any> : Deserializable<T> {
    override fun deserialize(response: Response): T {
        try {
            return deserialize(response.dataStream) ?: deserialize(response.dataStream.reader())
            ?: deserialize(response.data) ?: deserialize(String(response.data))
            ?: throw IllegalStateException("One of deserialize(ByteArray) or deserialize(InputStream) or deserialize(Reader) or deserialize(String) must be implemented")
        } finally {
            response.dataStream.close()
        }
    }

    //One of these methods must be implemented
    fun deserialize(bytes: ByteArray): T? = null

    fun deserialize(inputStream: InputStream): T? = null

    fun deserialize(reader: Reader): T? = null

    fun deserialize(content: String): T? = null
}

fun <T : Any, U : Deserializable<T>> Request.response(deserializable: U, handler: (Request, Response, Result<T, FuelError>) -> Unit): Request {
    response(deserializable, { _, response, value ->
        handler(this@response, response, Result.Success(value))
    }, { _, response, error ->
        handler(this@response, response, Result.Failure(error))
    })
    return this
}

fun <T : Any, U : Deserializable<T>> Request.response(deserializable: U, handler: Handler<T>): Request {
    response(deserializable, { request, response, value ->
        handler.success(request, response, value)
    }, { request, response, error ->
        handler.failure(request, response, error)
    })
    return this
}

private fun <T : Any, U : Deserializable<T>> Request.response(deserializable: U,
                                                              success: (Request, Response, T) -> Unit,
                                                              failure: (Request, Response, FuelError) -> Unit): Request {
    val asyncRequest = AsyncTaskRequest(taskRequest)

    asyncRequest.successCallback = { response ->
        val deliverable = Result.of<T, Exception> { deserializable.deserialize(response) }
        callback {
            deliverable.fold({
                success(this, response, it)
            }, {
                failure(this, response, FuelError(it))
            })
        }
    }

    asyncRequest.failureCallback = { error, response ->
        callback {
            failure(this, response, error)
        }
    }
    submit(asyncRequest)

    return this
}

fun <T : Any, U : Deserializable<T>> Request.response(deserializable: U): Triple<Request, Response, Result<T, FuelError>> {
    var response: Response? = null
    val result = Result.of<Response, Exception> { taskRequest.call() }
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

