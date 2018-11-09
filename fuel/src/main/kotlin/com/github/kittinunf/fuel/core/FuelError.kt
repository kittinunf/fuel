package com.github.kittinunf.fuel.core

import java.io.InterruptedIOException

private typealias StackTrace = Array<out StackTraceElement>

/**
 * Indicates a call to [FuelError.wrap] passing in a [FuelError]
 */
private class BubbleFuelError(inner: FuelError) : FuelError(inner, inner.response)

/**
 * Error wrapper for all caught [Throwable] in fuel
 *
 * @see FuelError.wrap
 *
 * @param exception [Throwable] the underlying cause
 * @param response [Response] the response, if any
 */
open class FuelError internal constructor(
    exception: Throwable,
    val response: Response = Response.error()
) : Exception(exception.message, exception) {
    init {
        stackTrace = buildRelativeStack(wrapped = this.stackTrace, cause = exception.stackTrace)
    }

    /**
     * Builds a stack for [wrapped] that is relative to [cause]
     */
    private fun buildRelativeStack(wrapped: StackTrace, cause: StackTrace): StackTrace {
        return wrapped
            .takeWhile { stack -> cause.find { inner -> inner == stack } == null }
            .toTypedArray()
    }

    /**
     * Returns a string representation of the error with the complete stack
     */
    override fun toString(): String = "${this::class.java.canonicalName}: $message\r\n".plus(buildString {
        stackTrace.forEach { stack -> appendln("\t$stack") }

        cause?.also {
            append("Caused by: ")
            appendln(it.toString())
            when (it) {
                is FuelError -> {}
                else -> { it.stackTrace.forEach { stack -> appendln("\t$stack") } }
            }
        }
    })

    /**
     * Get the original exception that caused this, passing through all [BubbleFuelError] and wrapping [FuelError]
     * @return [Throwable] the original exception
     */
    val exception: Throwable get() {
        var pointer: Throwable = this
        while (pointer is FuelError && pointer.cause != null) {
            pointer = pointer.cause!!
        }

        return pointer
    }

    /**
     * Get the [Response] error data
     *
     * @see Response.body
     * @return [ByteArray] the error data
     */
    val errorData: ByteArray get() = response.data

    /**
     * Returns if this [FuelError] was caused by an interruption
     * @return [Boolean] true if it was, false otherwise
     */
    val causedByInterruption: Boolean get() =
        exception is InterruptedException ||
        exception is InterruptedIOException

    companion object {
        fun wrap(it: Throwable, response: Response = Response.error()): FuelError = when (it) {
            is FuelError -> BubbleFuelError(it)
            else -> FuelError(it, response = response)
        }
    }
}
