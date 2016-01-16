package com.github.kittinunf.fuel

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

abstract class BaseTestCase {

    val DEFAULT_TIMEOUT = 15L

    lateinit var lock: CountDownLatch

    fun await(seconds: Long = DEFAULT_TIMEOUT) {
        lock.await(seconds, TimeUnit.SECONDS);
    }

}