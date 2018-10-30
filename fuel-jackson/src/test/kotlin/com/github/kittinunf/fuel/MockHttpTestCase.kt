package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Parameters
import com.github.kittinunf.fuel.core.Request
import org.junit.After
import org.junit.Before
import org.slf4j.event.Level

abstract class MockHttpTestCase {

    protected lateinit var mock: MockHelper

    init {
        Fuel.testMode {
            timeout = 15000
        }
    }

    @Before
    fun setup() {
        // You can set the log level to INFO or TRACE to see all the mocking logging
        this.mock = MockHelper()
        this.mock.setup(Level.INFO)
    }

    @After
    fun tearDown() {
        this.mock.tearDown()
    }

    fun reflectedRequest(
        method: Method,
        path: String,
        parameters: Parameters? = null,
        manager: FuelManager = FuelManager.instance
    ): Request {
        mock.chain(
            request = mock.request().withMethod(method.value).withPath("/$path"),
            response = mock.reflect()
        )

        return manager.request(method, mock.path(path), parameters)
    }
}