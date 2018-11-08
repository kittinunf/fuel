package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import java.io.InterruptedIOException
import java.util.concurrent.Callable

internal fun Request.toTask(): Callable<Response> = RequestTask(this)

internal class RequestTask(internal val request: Request) : Callable<Response> {
    private val interruptCallback by lazy { executor.interruptCallback }
    private val executor by lazy { request.executionOptions }
    private val client by lazy { executor.client }

    override fun call(): Response = runCatching {
        val modifiedRequest = executor.requestTransformer(request)
        val response = client.executeRequest(modifiedRequest)

        runCatching { executor.responseTransformer(modifiedRequest, response) }
            .getOrElse { error -> throw FuelError.wrap(error, response) }
    }.getOrElse { error: Throwable ->
        throw FuelError.wrap(error).also {
            (it.exception as? InterruptedIOException).also { interruptCallback?.invoke(request) }
        }
    }
}