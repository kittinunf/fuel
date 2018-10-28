package com.github.kittinunf.fuel.core

typealias ProgressCallback = (readBytes: Long, totalBytes: Long) -> Unit

data class Progress(val handlers: MutableCollection<ProgressCallback> = mutableListOf()) {
    fun add(handler: ProgressCallback): Progress {
        plusAssign(handler)
        return this
    }

    fun remove(handler: ProgressCallback): Progress {
        minusAssign(handler)
        return this
    }

    fun invoke(readBytes: Long, totalBytes: Long) {
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