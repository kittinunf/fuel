package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.core.requests.AsyncTaskRequest
import com.github.kittinunf.result.Result
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.util.concurrent.TimeUnit.MILLISECONDS

interface Deserializable<out T : Any> {
    fun deserialize(response: Response): T
}

interface ResponseDeserializable<out T : Any> : Deserializable<T> {
    override fun deserialize(response: Response): T {
        return deserialize(response.data) ?:
                deserialize(ByteArrayInputStream(response.data)) ?:
                deserialize(InputStreamReader(ByteArrayInputStream(response.data))) ?:
                deserialize(String(response.data)) ?:
                throw IllegalStateException("One of deserialize(ByteArray) or deserialize(InputStream) or deserialize(Reader) or deserialize(String) must be implemented")
    }

    //One of these methods must be implemented
    fun deserialize(bytes: ByteArray): T? = null

    fun deserialize(inputStream: InputStream): T? = null

    fun deserialize(reader: Reader): T? = null

    fun deserialize(content: String): T? = null
}

fun <T : Any, U : Deserializable<T>> Request.response(deserializable: U, handler: (Request, Response, Result<T, FuelError>) -> Unit): Request {
    response(deserializable, { request, response, value ->
        handler(this@response, response, Result.Success(value))
    }, { request, response, error ->
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
    taskRequest.validating = false
    val request = AsyncTaskRequest(taskRequest)
    request.apply {
        validator = taskRequest.validator
        successCallback = { response ->
            val deliverable = Result.of { deserializable.deserialize(response) }
            callback {
                deliverable.fold({
                    success(this@response, response, it)
                }, {
                    failure(this@response, response, FuelError().apply { exception = it })
                })
            }
        }

        failureCallback = { error, response ->
            callback {
                failure(this@response, response, error)
            }
        }
    }

    submit(request)
    return this
}

fun <T : Any, U: Deserializable<T>> Request.response(deserializable: U): Triple<Request, Response, T>  {
    val response = taskRequest.call()
    return Triple(this, response ,deserializable.deserialize(response))
}