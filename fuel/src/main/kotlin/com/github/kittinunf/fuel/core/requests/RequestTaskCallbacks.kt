package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import java.util.concurrent.Callable

internal typealias RequestSuccessCallback = ((Response) -> Unit)
internal typealias RequestFailureCallback = ((FuelError, Response) -> Unit)
internal class RequestTaskCallbacks(
    private val request: Request,
    private val onSuccess: RequestSuccessCallback,
    private val onFailure: RequestFailureCallback
) : Callable<Response> {
    override fun call(): Response = try {
        request.toTask().call().also {
            onSuccess(it)
        }
    } catch (error: FuelError) {
        onFailure(error, error.response)
        errorResponse()
    } catch (ex: Exception) {
        val error = FuelError(ex)
        val response = errorResponse()
        onFailure(error, response)
        response
    }

    private fun errorResponse() = Response(request.url)
}
