package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelError
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not
import org.junit.Assert.assertThat
import org.junit.Test


class ErrorTest: BaseTestCase() {
    @Test
    fun testNoMessageException() {
        val error = FuelError().apply {
            exception = RuntimeException()
        }

        assertThat(error.toString(), containsString("<no message>"))
        assertThat(error.toString(), not(containsString("null")))
        assertThat(error.toString(), containsString("RuntimeException"))
    }

    @Test
    fun testMessageException() {
        val error = FuelError().apply {
            exception = RuntimeException("error")
        }

        assertThat(error.toString(), containsString("error"))
        assertThat(error.toString(), not(containsString("<no message>")))
    }
}
