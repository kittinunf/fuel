package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import java.util.concurrent.Callable

internal class AsyncTaskRequest(private val request: Request) : Callable<Response> {
    var successCallback: ((Response) -> Unit)? = null
    var failureCallback: ((FuelError, Response) -> Unit)? = null

    override fun call(): Response = try {
        request.toTask().call().also {
            successCallback?.invoke(it)
        }
    } catch (error: FuelError) {
        failureCallback?.invoke(error, error.response)
        errorResponse()
    } catch (ex: Exception) {
        val error = FuelError(ex)
        val response = errorResponse()
        failureCallback?.invoke(error, response)
        response
    }

    private fun errorResponse() = Response(request.url)
}
