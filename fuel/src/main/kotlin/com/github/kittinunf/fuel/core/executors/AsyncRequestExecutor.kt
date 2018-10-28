package com.github.kittinunf.fuel.core.executors

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import java.util.concurrent.Future

internal class AsyncRequestExecutor(
    private val wrapped: RequestExecutor
) : RequestExecutor by wrapped {
    lateinit var successCallback: ((Response) -> Unit)
    lateinit var failureCallback: ((FuelError, Response) -> Unit)

    override fun call(): Response = try {
        wrapped.call().apply {
            successCallback.invoke(this)
        }
    } catch (error: FuelError) {
        failureCallback.invoke(error, error.response)
        errorResponse()
    } catch (ex: Exception) {
        val error = FuelError(ex)
        val response = errorResponse()
        failureCallback.invoke(error, response)
        response
    }

    fun callback(f: () -> Unit) {
        callbackExecutor.execute {
            f.invoke()
        }
    }

    fun submit(): Future<*> {
        return submit(this)
    }

    private fun errorResponse() = Response(request.url)
}

class CancellableRequest(override val request: Request, private val future: Future<*>) : Request by request {
    fun cancel(interruptible: Boolean = true) {
        future.cancel(interruptible)
    }

    companion object {
        fun from(request: Request, future: Future<*>): CancellableRequest {
            return CancellableRequest(request, future)
        }
    }
}