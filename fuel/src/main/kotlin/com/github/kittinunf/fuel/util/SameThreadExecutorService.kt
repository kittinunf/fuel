package com.github.kittinunf.fuel.util

import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.TimeUnit

/**
 * Executes all submitted tasks directly in the same thread as the caller.
 */
class SameThreadExecutorService : AbstractExecutorService() {

    //volatile because can be viewed by other threads
    @Volatile private var terminated: Boolean = false

    override fun shutdown() {
        terminated = true
    }

    override fun isShutdown(): Boolean {
        return terminated
    }

    override fun isTerminated(): Boolean {
        return terminated
    }

    @Throws(InterruptedException::class)
    override fun awaitTermination(theTimeout: Long, theUnit: TimeUnit): Boolean {
        shutdown()
        return terminated
    }

    override fun shutdownNow(): List<Runnable> {
        return emptyList()
    }

    override fun execute(theCommand: Runnable) {
        theCommand.run()
    }
}
