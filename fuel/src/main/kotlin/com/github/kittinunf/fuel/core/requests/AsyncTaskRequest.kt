package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Response

class AsyncTaskRequest(val task: TaskRequest) : TaskRequest(task.request) {

    var successCallback: ((Response) -> Unit)? = null
    var failureCallback: ((FuelError, Response) -> Unit)? = null

    override fun call(): Response {
        return try {
            val response = task.call()
            response.apply { successCallback?.invoke(this) }
        } catch(error: FuelError) {
            failureCallback?.invoke(error, error.response)
            return errorResponse()
        } catch(ex: Exception) {
            val error = FuelError().apply {
                exception = ex
            }
            val response = errorResponse()
            failureCallback?.invoke(error, response)
            return response
        }
    }

    private fun errorResponse() = Response().apply { url = request.url }

}