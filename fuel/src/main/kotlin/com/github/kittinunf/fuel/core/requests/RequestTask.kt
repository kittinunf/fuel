package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.HttpException
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import java.util.concurrent.Callable

private typealias RequestTaskResult = Pair<Request, Response>
/**
 * Synchronous version of [SuspendableRequest]. Turns a [Request] into a [Callable]
 */
internal class RequestTask(internal val request: Request) : Callable<Response> {
    private val interruptCallback by lazy { executor.interruptCallback }
    private val executor by lazy { request.executionOptions }
    private val client by lazy { executor.client }

    private fun prepareRequest(request: Request): Request = executor.requestTransformer(request)

    @Throws(FuelError::class)
    private fun executeRequest(request: Request): RequestTaskResult {
        return runCatching { Pair(request, client.executeRequest(request)) }
            .recover { error -> throw FuelError.wrap(error, Response(request.url)) }
            .getOrThrow()
    }

    @Throws(FuelError::class)
    private fun prepareResponse(result: RequestTaskResult): Response {
        val (request, response) = result
        return runCatching { executor.responseTransformer(request, response) }
            .mapCatching {
                val valid = executor.responseValidator(it)
                if (valid) it else throw HttpException(response.statusCode, response.responseMessage)
            }
            .recover { error -> throw FuelError.wrap(error, response) }
            .getOrThrow()
    }

    @Throws(FuelError::class)
    override fun call(): Response {
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
                Fuel.trace { "[RequestTask] on failure (interrupted=${(error as? FuelError)?.causedByInterruption ?: error})" }
                if (error is FuelError && error.causedByInterruption) {
                    Fuel.trace { "[RequestTask] execution error\n\r\t$error" }
                    interruptCallback.invoke(request)
                }
            }
            .getOrThrow()
    }
}

internal fun Request.toTask(): Callable<Response> = RequestTask(this)
