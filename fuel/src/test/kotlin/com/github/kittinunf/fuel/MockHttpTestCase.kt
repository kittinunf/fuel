package com.github.kittinunf.fuel

import org.junit.After
import org.junit.Before

abstract class MockHttpTestCase : BaseTestCase() {

    protected lateinit var mock: MockHelper

    @Before
    fun setup() {
        this.mock = MockHelper()
        this.mock.setup()
    }

    @After
    fun tearDown() {
        this.mock.tearDown()
    }
}