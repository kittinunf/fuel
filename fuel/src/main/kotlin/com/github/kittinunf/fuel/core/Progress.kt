package com.github.kittinunf.fuel.core

typealias ProgressCallback = (readBytes: Long, totalBytes: Long) -> Unit

data class Progress(private val handlers: MutableCollection<ProgressCallback> = mutableListOf()) : ProgressCallback {
    fun add(vararg handlers: ProgressCallback): Progress {
        handlers.forEach { handler -> plusAssign(handler) }
        return this
    }

    fun remove(handler: ProgressCallback): Progress {
        minusAssign(handler)
        return this
    }

    fun isNotSet(): Boolean = handlers.isEmpty()

    override operator fun invoke(readBytes: Long, totalBytes: Long) {
        handlers.forEach {
            it.invoke(readBytes, totalBytes)
        }
    }

    operator fun plusAssign(handler: ProgressCallback) {
        handlers.add(handler)
    }

    operator fun minusAssign(handler: ProgressCallback) {
        handlers.remove(handler)
    }
}
