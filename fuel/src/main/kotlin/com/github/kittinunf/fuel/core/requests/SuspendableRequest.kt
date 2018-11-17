package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result

/**
 * Coroutine version of [RequestTask]. Turns a [Request] into an executable, suspendable, coroutine.
 */
class SuspendableRequest private constructor(private val wrapped: Request) : Request by wrapped {
    private val interruptCallback by lazy { executor.interruptCallback }
    private val executor by lazy { request.executionOptions }
    private val client by lazy { executor.client }

    private fun prepareRequest(request: Request): Request = executor.requestTransformer(request)

    private suspend fun executeRequest(request: Request): Pair<Request, Response> {
        return runCatching { Pair(request, client.awaitRequest(request)) }
            .recover { error -> throw FuelError.wrap(error, Response(url)) }
            .getOrThrow()
    }

    private fun prepareResponse(result: Pair<Request, Response>): Response {
        val (request, response) = result
        return runCatching { executor.responseTransformer(request, response) }
            .recover { error -> throw FuelError.wrap(error, response) }
            .getOrThrow()
    }

    suspend fun awaitResult(): Result<Response, FuelError> {
        return runCatching { prepareRequest(request) }
            .mapCatching { executeRequest(it) }
            .mapCatching { pair ->
                // Nested runCatching so response can be rebound
                runCatching { prepareResponse(pair) }
                    .recover { error ->
                        error.also { Fuel.trace { "[RequestTask] execution error\n\r\t$error" } }
                        throw FuelError.wrap(error, pair.second)
                    }
                    .getOrThrow()
            }
            .onFailure { error ->
                Fuel.trace { "[RequestTask] on failure ${(error as? FuelError)?.exception ?: error}" }
                if (error is FuelError && error.causedByInterruption) {
                    Fuel.trace { "[RequestTask] execution error\n\r\t$error" }
                    interruptCallback.invoke(request)
                }
            }
            .map { Result.Success<Response, FuelError>(it) }
            .recover { Result.Failure<Response, FuelError>(it as FuelError) }
            .getOrThrow()
    }

    @Throws(FuelError::class)
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