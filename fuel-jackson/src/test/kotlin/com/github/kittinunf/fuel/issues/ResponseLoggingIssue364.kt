package com.github.kittinunf.fuel.issues

import com.github.kittinunf.fuel.MockHttpTestCase
import com.github.kittinunf.fuel.MockReflected
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.interceptors.loggingResponseInterceptor
import com.github.kittinunf.fuel.jackson.jacksonDeserializerOf
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class ResponseLoggingIssue364 : MockHttpTestCase() {

    private val outContent = ByteArrayOutputStream()
    private val errContent = ByteArrayOutputStream()
    private val originalOut = System.out
    private val originalErr = System.err

    @Before
    fun prepareStreams() {
        System.setOut(PrintStream(outContent))
        System.setErr(PrintStream(errContent))
    }

    @After
    fun teardownStreams() {
        System.setOut(originalOut)
        System.setErr(originalErr)

        System.out.print(outContent)
        System.err.print(errContent)
    }

    private val threadSafeManager = FuelManager()
    private val responseLoggingInterceptor = loggingResponseInterceptor()

    @Before
    fun addBodyInterceptor() {
        threadSafeManager.addResponseInterceptor { responseLoggingInterceptor }
    }

    @After
    fun removeBodyInterceptor() {
        threadSafeManager.removeResponseInterceptor { responseLoggingInterceptor }
    }

    @Test
    fun responseLoggingWorksWithDeserialization() {
        val value = "foobarbaz"
        val request = reflectedRequest(Method.POST, "logged-response-body", manager = threadSafeManager)
                .body(value)

        val (_, response, result) = request.responseObject(jacksonDeserializerOf<MockReflected>())
        val (reflected, error) = result

        assertThat(error, CoreMatchers.nullValue())
        assertThat(reflected, CoreMatchers.notNullValue())
        assertThat(reflected!!.body?.string, equalTo(value))

        // Check that the response was actually logged
        val loggedOutput = String(outContent.toByteArray())
        assertThat(loggedOutput.length, not(equalTo(0)))
        assertThat(loggedOutput, containsString("\"body\":"))
        assertThat(loggedOutput, containsString(value))
    }
}