package com.github.kittinunf.fuel.android

import org.junit.runner.RunWith
import org.robolectric.RobolectricGradleTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Created by Kittinun Vantasin on 11/9/15.
 */

@RunWith(RobolectricGradleTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(21))
abstract class BaseTestCase {

    val DEFAULT_TIMEOUT = 15L

    lateinit var lock: CountDownLatch

    fun await(seconds: Long = DEFAULT_TIMEOUT) {
        lock.await(seconds, TimeUnit.SECONDS);
    }

}
