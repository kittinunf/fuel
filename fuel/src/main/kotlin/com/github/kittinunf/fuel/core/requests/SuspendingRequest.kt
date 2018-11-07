package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError
import java.io.InterruptedIOException

class SuspendingRequest(private val request: Request) {
    var interruptCallback: ((Request) -> Unit)? = null
    private val executor by lazy { request.executionOptions }

    suspend fun awaitResult(): Result<Response, FuelError> {
        val modifiedRequest = executor.requestTransformer(request)
        val response = executor.client.awaitRequest(modifiedRequest)

        return Result.of<Response, FuelError> {
            executor.responseTransformer(modifiedRequest, response)
        }.mapError { e ->
            val error = e as? FuelError ?: FuelError(e)
            if (error.exception as? InterruptedIOException != null) {
                interruptCallback?.invoke(request)
            }
            error
        }
    }
}