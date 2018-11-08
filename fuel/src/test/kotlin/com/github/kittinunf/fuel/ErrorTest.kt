package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelError
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not
import org.junit.Assert.assertThat
import org.junit.Test

class ErrorTest {
    @Test
    fun testMessageException() {
        val error = FuelError(RuntimeException("error"))
        assertThat(error.toString(), containsString("error"))
        assertThat(error.toString(), not(containsString("<no message>")))
    }
}
