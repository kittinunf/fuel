package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import java.util.concurrent.Future

class CancellableRequest(
    private val wrapped: Request,
    val future: Future<Response>
) : Request by wrapped, Future<Response> by future {
    override val request: CancellableRequest = this
    override fun toString() = "Cancellable[\n\r\t$request\n\r] done=$isDone cancelled=$isCancelled"

    fun cancel() = cancel(true)
    fun join(): Response = future.get()
}