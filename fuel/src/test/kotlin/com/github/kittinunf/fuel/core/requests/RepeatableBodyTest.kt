package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.Method
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URL

class RepeatableBodyTest {

    @Test
    fun repeatableBodyIsNeverConsumed() {
        val body = DefaultBody.from({ ByteArrayInputStream("body".toByteArray()) }, { 4 }).asRepeatable()
        assertThat(body.isConsumed(), equalTo(false))
        body.writeTo(ByteArrayOutputStream())
        assertThat(body.isConsumed(), equalTo(false))
    }

    @Test
    fun byteArrayBodyIsRepeatable() {
        val value = ByteArray(32).apply {
            for (i in 0..(this.size - 1)) {
                this[i] = ('A'..'z').random().toByte()
            }
        }

        DefaultRequest(Method.POST, URL("https://test.fuel.com/"))
            .body(value)
            .apply {
                val output = ByteArrayOutputStream(value.size)
                assertThat(body.length?.toInt(), equalTo(value.size))
                assertThat(body.toByteArray(), equalTo(value))

                body.writeTo(output)
                assertThat(output.toString(), equalTo(String(value)))
                assertThat(body.isConsumed(), equalTo(false))
            }
    }

    @Test
    fun stringBodyIsRepeatable() {
        val value = "body"
        DefaultRequest(Method.POST, URL("https://test.fuel.com/"))
            .body(value)
            .apply {
                val output = ByteArrayOutputStream(value.length)
                assertThat(body.length?.toInt(), equalTo(value.length))
                assertThat(body.toByteArray(), equalTo(value.toByteArray()))

                body.writeTo(output)
                assertThat(output.toString(), equalTo(value))
                assertThat(body.isConsumed(), equalTo(false))
            }
    }

    @Test
    fun requestWithRepeatableBodyIsPrintableAfterConsumption() {
        val value = "String Body ${Math.random()}"

        DefaultRequest(Method.POST, URL("https://test.fuel.com/"))
            .body(value)
            .apply {
                val output = ByteArrayOutputStream()
                body.writeTo(output)

                assertThat(this.toString(), CoreMatchers.containsString(value))
            }
    }
}