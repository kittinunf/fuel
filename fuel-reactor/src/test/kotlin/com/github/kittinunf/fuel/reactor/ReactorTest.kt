package com.github.kittinunf.fuel.reactor

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import reactor.test.test

class ReactorTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun enableFuelTestingMode() {
            Fuel.testMode { timeout = 5000 }
            FuelManager.instance.basePath = "https://httpbin.org"
        }
    }

    @Test
    fun streamBytes() {
        Fuel.get("/ip").monoOfBytes()
            .test()
            .assertNext { assertTrue(it.size > 0) }
            .verifyComplete()
    }

    @Test
    fun streamString() {
        Fuel.get("/uuid").monoOfString()
            .test()
            .assertNext { assertTrue(it.isNotEmpty()) }
            .verifyComplete()
    }
}
