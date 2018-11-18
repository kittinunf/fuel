package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import java.util.concurrent.Future

/**
 * Request extension that adds [cancel] to a Running or Pending [Request].
 *
 * @see [com.github.kittinunf.fuel.core.Deserializable] used when using handlers
 *
 * @param wrapped [Request] the request that will be running
 * @param future [Future<Response>] the running or pending request execution that will yield a [Response]
 */
class CancellableRequest private constructor(private val wrapped: Request, private val future: Future<Response>) :
    Request by wrapped, Future<Response> by future {
    private val interruptCallback by lazy { executor.interruptCallback }
    private val executor by lazy { request.executionOptions }

    override val request: CancellableRequest = this
    override fun toString() = "Cancellable[\n\r\t$wrapped\n\r] done=$isDone cancelled=$isCancelled"

    /**
     * Cancel the request, interrupt if in progress
     */
    fun cancel() = future.cancel(true)

    /**
     * Wait for the request to be finished, error-ed, cancelled or interrupted
     * @return [Response]
     */
    fun join(): Response = runCatching { future.get() }.fold(
        onSuccess = { it -> it.also { Fuel.trace { "[CancellableRequest] joined to $it" } } },
        onFailure = { error ->
            Response.error(url).also {
                Fuel.trace { "[CancellableRequest] joined to $error" }
                if (FuelError.wrap(error).causedByInterruption) {
                    interruptCallback.invoke(wrapped)
                }
            }
        }
    )

    companion object {
        val FEATURE: String = CancellableRequest::class.java.canonicalName
        fun enableFor(request: Request, future: Future<Response>): CancellableRequest {
            // Makes sure the "newest" request is stored, although it should always be the same.
            val current = getFor(request) ?: CancellableRequest(request, future)
            if (request !== current) {
                request.enabledFeatures[FEATURE] = current
            }

            return current
        }

        fun getFor(request: Request): CancellableRequest? {
            return request.enabledFeatures[FEATURE] as? CancellableRequest
        }
    }
}

/**
 * Tries to cancel the request.
 *
 * @note Not all [Request] can be cancelled, so this may fail without reason.

 * @param mayInterruptIfRunning [Boolean] if the thread executing this task should be interrupted; otherwise,
 *   in-progress tasks are allowed to complete.
 * @return [Boolean] true if it was cancelled, false otherwise
 */
fun Request.tryCancel(mayInterruptIfRunning: Boolean = true): Boolean {
    val feature = request.enabledFeatures[CancellableRequest.FEATURE] as? CancellableRequest
    return feature?.cancel(mayInterruptIfRunning) ?: false
}

/**
 * Get the current cancellation state
 *
 * @note This can be used in code which may not be interrupted but has certain break points where it can be interrupted.
 * @return [Boolean] true if cancelled, false otherwise
 */
val Request.isCancelled: Boolean get() = CancellableRequest.getFor(request)?.isCancelled ?: false