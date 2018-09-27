package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError
import java.io.InterruptedIOException
import java.util.concurrent.Callable

internal open class TaskRequest(internal val request: Request) : Callable<Response> {
    var interruptCallback: ((Request) -> Unit)? = null

    override fun call(): Response = try {
        val modifiedRequest = request.requestInterceptor?.invoke(request) ?: request
        val response = request.client.executeRequest(modifiedRequest)

        request.responseInterceptor?.invoke(modifiedRequest, response) ?: response
    } catch (error: FuelError) {
        if (error.exception as? InterruptedIOException != null) {
            interruptCallback?.invoke(request)
        }
        throw error
    } catch (exception: Exception) {
        throw FuelError(exception)
    }

    open suspend fun awaitResult(): Result<Response, FuelError> {
        return try {
            val modifiedRequest = request.requestInterceptor?.invoke(request) ?: request
            var response = request.client.awaitRequest(modifiedRequest)
            response = request.responseInterceptor?.invoke(modifiedRequest, response) ?: response
            Result.of(response).mapError { it as? FuelError ?: FuelError(it) }
        } catch (e: Exception) {
            val error = e as? FuelError ?: FuelError(e)
            if (error.exception as? InterruptedIOException != null) {
                interruptCallback?.invoke(request)
            }
            Result.error(error)
        }
    }
}