package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import java.util.concurrent.Callable

internal typealias RequestSuccessCallback = ((Response) -> Unit)
internal typealias RequestFailureCallback = ((FuelError, Response) -> Unit)

/**
 * Wraps a [task] with callbacks [onSuccess] and [onFailure]
 *
 * @param request [Request] the request that generated the task
 * @param task [Callable<Response>] the task to execute (and perform callbacks on)
 * @param onSuccess [RequestSuccessCallback] the success callback, called when everything went fine
 * @param onFailure [RequestFailureCallback] the failure callback, called when an error occurred
 */
internal class RequestTaskCallbacks(
    private val request: Request,
    private val task: Callable<Response> = request.toTask(),
    private val onSuccess: RequestSuccessCallback,
    private val onFailure: RequestFailureCallback
) : Callable<Response> {
    override fun call(): Response {
        println("[RequestTaskCallbacks] start request task\n\r\t$request")
        return runCatching { task.call() }
            .mapCatching { it -> it.also { onSuccess(it) } }
            .getOrElse { error -> FuelError.wrap(error).also { onFailure(it, it.response) }.response }
    }
}
