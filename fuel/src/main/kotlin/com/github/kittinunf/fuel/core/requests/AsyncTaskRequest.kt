package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.HttpException
import com.github.kittinunf.fuel.core.Response
import java.io.InterruptedIOException

class AsyncTaskRequest(val task: TaskRequest) : TaskRequest(task.request) {
    var successCallback: ((Response) -> Unit)? = null
    var failureCallback: ((FuelError, Response) -> Unit)? = null

    override fun call(): Response {
        try {
            val response = task.call()
            dispatchCallback(response)
            return response
        } catch(error: FuelError) {
            if (error.exception as? InterruptedIOException != null) {
                interruptCallback?.invoke(request)
            } else {
                val response = Response()
                response.url = request.url
                failureCallback?.invoke(error, response)
            }
        } catch(ex: Exception) {
            val temp = ex
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

    override fun dispatchCallback(response: Response) {
        //validate
        if (validator.invoke(response)) {
            successCallback?.invoke(response)
        } else {
            val error = FuelError().apply {
                exception = HttpException(response.httpStatusCode, response.httpResponseMessage)
                errorData = response.data
            }
            failureCallback?.invoke(error, response)
        }
    }
}