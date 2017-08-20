package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Response

internal class AsyncTaskRequest(private val task: TaskRequest) : TaskRequest(task.request) {
    var successCallback: ((Response) -> Unit)? = null
    var failureCallback: ((FuelError, Response) -> Unit)? = null

    override fun call(): Response = try {
        task.call().apply {
            successCallback?.invoke(this)
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