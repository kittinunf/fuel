package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.HttpException
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.failure
import com.github.kittinunf.result.success
import java.io.InterruptedIOException

class AsyncTaskRequest(val task: TaskRequest) : TaskRequest(task.request) {
    var successCallback: ((Response) -> Unit)? = null
    var failureCallback: ((FuelError, Response) -> Unit)? = null

    override fun call(): Result<Response, FuelError> {
        try {
            val call = task.call()
            call.success {
                dispatchCallback(it)
            }
            call.failure { error ->
                if (error.exception as? InterruptedIOException != null) {
                    interruptCallback?.invoke(request)
                } else {
                    val response = Response()
                    response.url = request.url
                    failureCallback?.invoke(error, response)
                }
            }
            return Result.Success(Response()) //FIXME
        } catch(ex: Exception) {
            val error = FuelError().apply {
                exception = ex
            }
            val response = Response()
            response.url = request.url
            failureCallback?.invoke(error, response)
            return Result.error(error)
        }
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