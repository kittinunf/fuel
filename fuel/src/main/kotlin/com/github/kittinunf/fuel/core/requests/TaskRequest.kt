package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.*
import java.io.InterruptedIOException
import java.util.concurrent.Callable

open class TaskRequest(val request: Request) : Callable<Response> {
    var validator: (Response) -> Boolean = { response ->
        (200..299).contains(response.httpStatusCode)
    }
    var interruptCallback: ((Request) -> Unit)? = null

    var validating = true

    override fun call(): Response {
        try {
            return Manager.instance.client.executeRequest(request).apply { dispatchCallback(this) }
        } catch(error: FuelError) {
            if (error.exception as? InterruptedIOException != null) {
                interruptCallback?.invoke(request)
            }
            throw error
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