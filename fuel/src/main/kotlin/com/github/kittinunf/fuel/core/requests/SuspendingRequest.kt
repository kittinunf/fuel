package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError
import java.io.InterruptedIOException

class SuspendingRequest(private val request: Request) {
    var interruptCallback: ((Request) -> Unit)? = null

    suspend fun awaitResult(): Result<Response, FuelError> {
        val modifiedRequest = request.requestTransformer.invoke(request)
        val response = request.client.awaitRequest(modifiedRequest)

        return Result.of<Response, FuelError> {
            request.responseTransformer.invoke(modifiedRequest, response)
        }.mapError { e ->
            val error = e as? FuelError ?: FuelError(e)
            if (error.exception as? InterruptedIOException != null) {
                interruptCallback?.invoke(request)
            }
            error
        }
    }
}