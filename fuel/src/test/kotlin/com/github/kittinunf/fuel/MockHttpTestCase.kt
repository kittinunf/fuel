package com.github.kittinunf.fuel

import org.junit.After
import org.junit.Before
import org.slf4j.event.Level

abstract class MockHttpTestCase : BaseTestCase() {

    protected lateinit var mock: MockHelper

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
}