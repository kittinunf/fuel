package com.github.kittinunf.fuel.core

import java.io.InterruptedIOException

private class BubbleFuelError(inner: FuelError) : FuelError(inner, inner.response)

open class FuelError internal constructor(
    exception: Throwable,
    val response: Response = Response.error()
) : Exception(exception.message, exception) {
    init {
        // Only store new stack since `cause`
        stackTrace = stackTrace.takeWhile { stack -> exception.stackTrace.find { inner -> inner == stack } == null }
            .toTypedArray()
    }

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

    val exception: Throwable get() {
        var pointer: Throwable = this
        while (pointer is FuelError && pointer.cause != null) {
            pointer = pointer.cause!!
        }

        return pointer
    }

    val errorData: ByteArray get() = response.data

    /**
     * This FuelError was caused by an interruption
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
