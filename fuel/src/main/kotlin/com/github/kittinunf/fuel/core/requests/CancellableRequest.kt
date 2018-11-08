package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import java.util.concurrent.Future

class CancellableRequest(
    private val wrapped: Request,
    val future: Future<Response>
) : Request by wrapped, Future<Response> by future {
    private val interruptCallback by lazy { executor.interruptCallback }
    private val executor by lazy { request.executionOptions }

    override val request: CancellableRequest = this
    override fun toString() = "Cancellable[\n\r\t$wrapped\n\r] done=$isDone cancelled=$isCancelled"

    fun cancel() = future.cancel(true)
    fun join(): Response? = runCatching { future.get() }.fold(
        onSuccess = { it -> it },
        onFailure = { Response.error().also { interruptCallback?.invoke(wrapped) } }
    )
}