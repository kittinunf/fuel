package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.result.Result
import java.io.InterruptedIOException
import java.util.concurrent.Callable

open class TaskRequest(val request: Request) : Callable<Result<Response, FuelError>> {
    var validator: (Response) -> Boolean = { response ->
        (200..299).contains(response.httpStatusCode)
    }
    var interruptCallback: ((Request) -> Unit)? = null

    var validating = true

    override fun call(): Result<Response, FuelError> {
        try {
            return Result.Success(Manager.instance.client.executeRequest(request).apply { dispatchCallback(this) })
        } catch(error: FuelError) {
            if (error.exception as? InterruptedIOException != null) {
                interruptCallback?.invoke(request)
            }
            return Result.error(error)
        }
    }

    open fun dispatchCallback(response: Response) {
        if (validating) {
            //validate
            if (!validator(response)) {
                val error = FuelError().apply {
                    exception = HttpException(response.httpStatusCode, response.httpResponseMessage)
                    errorData = response.data
                }
                throw error
            }
        }
    }
}