package com.github.kittinunf.fuel.core.executors

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError
import java.io.InterruptedIOException

internal class SuspendingRequestExecutor(
    private val wrapped: RequestExecutor
) : RequestExecutor by wrapped {

    suspend fun awaitResult(): Result<Response, FuelError> {
        val modifiedRequest = requestTransformer.invoke(request)
        val response = client.awaitRequest(modifiedRequest)

        return Result.of<Response, FuelError> {
            responseTransformer.invoke(modifiedRequest, response)
        }.mapError { e ->
            val error = e as? FuelError ?: FuelError(e)
            if (error.exception as? InterruptedIOException != null) {
                interruptCallback?.invoke(request)
            }
            error
        }
    }
}