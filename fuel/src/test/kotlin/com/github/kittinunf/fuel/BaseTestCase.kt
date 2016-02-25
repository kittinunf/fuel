package com.github.kittinunf.fuel

abstract class BaseTestCase {

    init {
        enableTestMode()
    }

    protected inline fun regularMode(runnable: () -> Unit) {
        disableTestMode()
        runnable()
        enableTestMode()
    }

    protected fun enableTestMode() {
        Fuel.testMode {
            timeout = 15000
        }
    }

    protected fun disableTestMode() {
        Fuel.regularMode()
    }

}