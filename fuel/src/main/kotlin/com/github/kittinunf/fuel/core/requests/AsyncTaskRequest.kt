package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Response

class AsyncTaskRequest(val task: TaskRequest) : TaskRequest(task.request) {

    var successCallback: ((Response) -> Unit)? = null
    var failureCallback: ((FuelError, Response) -> Unit)? = null

    override fun call(): Response {
        try {
            val response = task.call()
            return response.apply { successCallback?.invoke(this) }
        } catch(error: FuelError) {
            failureCallback?.invoke(error, error.response)
        } catch(ex: Exception) {
            val error = FuelError().apply {
                exception = ex
            }
            val response = Response()
            response.url = request.url
            failureCallback?.invoke(error, response)
        }
        // FIXME
        return Response()
    }

}