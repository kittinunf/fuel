package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError
import java.io.InterruptedIOException

class SuspendableRequest(private val wrapped: Request) : Request by wrapped {
    var interruptCallback: ((Request) -> Unit)? = null
    private val executor by lazy { request.executionOptions }

    suspend fun awaitResult(): Result<Response, FuelError> {
        val modifiedRequest = executor.requestTransformer(request)
        val response = executor.client.awaitRequest(modifiedRequest)

        return Result.of<Response, Exception> {
            request.executionOptions.responseTransformer(modifiedRequest, response)
        }.mapError { e ->
            FuelError.wrap(e).also {
                if (it.exception as? InterruptedIOException != null) {
                    interruptCallback?.invoke(request)
                }
            }
        }
    }

    suspend fun await(): Response {
        return awaitResult().get()
    }

    companion object {
        private val FEATURE = SuspendableRequest::class.java.canonicalName
        fun enableFor(request: Request): SuspendableRequest {
            // Makes sure the "newest" request is stored, although it should always be the same.
            val current = request.enabledFeatures[FEATURE] ?: SuspendableRequest(request)
            if (request !== current) {
                request.enabledFeatures[FEATURE] = current
            }

            return current as SuspendableRequest
        }
    }
}

fun Request.suspendable() = SuspendableRequest.enableFor(this)