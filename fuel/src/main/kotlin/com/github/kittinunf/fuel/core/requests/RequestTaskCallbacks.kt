package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import java.util.concurrent.Callable

internal typealias RequestSuccessCallback = ((Response) -> Unit)
internal typealias RequestFailureCallback = ((FuelError, Response) -> Unit)
internal class RequestTaskCallbacks(
    private val request: Request,
    private val onSuccess: RequestSuccessCallback,
    private val onFailure: RequestFailureCallback
) : Callable<Response> {
    override fun call(): Response = runCatching { request.toTask().call().also { onSuccess(it) } }
        .getOrElse { error -> FuelError.wrap(error).also { onFailure(it, it.response) }.response }
}
