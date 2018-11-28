package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.core.requests.DefaultBody
import com.github.kittinunf.fuel.core.requests.DefaultRequest
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URL

class BodyTest {

    @Test
    fun bodyIsEmptyByDefault() {
        val body = DefaultBody()
        assertThat(body.isEmpty(), equalTo(true))
        assertThat(body.isConsumed(), equalTo(false))

        val request = DefaultRequest(Method.POST, URL("https://test.fuel.com/"))
        assertThat(request.toString(), containsString("(empty)"))
    }

    @Test
    fun bodyIsConsumedAfterWriting() {
        val body = DefaultBody.from({ ByteArrayInputStream("body".toByteArray()) }, { 4 })
        assertThat(body.isConsumed(), equalTo(false))

        body.writeTo(ByteArrayOutputStream())

        assertThat(body.isConsumed(), equalTo(true))
    }

    @Test
    fun bodyFromString() {
        val value = "String Body ${Math.random()}"

        DefaultRequest(Method.POST, URL("https://test.fuel.com/"))
            .body(value)
            .apply {
                val output = ByteArrayOutputStream(value.length)
                assertThat(body.length?.toInt(), equalTo(value.length))
                assertThat(body.toByteArray(), equalTo(value.toByteArray()))

                body.writeTo(output)
                assertThat(output.toString(), equalTo(value))
            }
    }

    @Test
    fun bodyFromByteArray() {
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
                assertArrayEquals(value, body.toByteArray())

                body.writeTo(output)
                assertArrayEquals(value, output.toByteArray())
            }
    }

    @Test
    fun bodyFromFile() {
        val value = "String Body ${Math.random()}"
        val file = File.createTempFile("BodyTest", ".txt")
        file.writeText(value)

        DefaultRequest(Method.POST, URL("https://test.fuel.com/"))
            .body(file)
            .apply {
                val output = ByteArrayOutputStream(value.length)

                assertThat(body.length?.toInt(), equalTo(value.length))
                assertThat(body.toByteArray(), equalTo(value.toByteArray()))

                body.writeTo(output)
                assertThat(output.toString(), equalTo(value))
            }
    }

    @Test
    fun bodyFromStream() {
        val value = "String Body ${Math.random()}"
        val stream = ByteArrayInputStream(value.toByteArray())

        DefaultRequest(Method.POST, URL("https://test.fuel.com/"))
            .body(stream)
            .apply {
                val output = ByteArrayOutputStream(value.length)
                assertThat(body.toByteArray(), equalTo(value.toByteArray()))

                body.writeTo(output)
                assertThat(output.toString(), equalTo(value))
            }
    }

    @Test(expected = FuelError::class)
    fun bodyFromCallbackCanOnlyBeReadOnce() {
        val body = DefaultBody.from({ ByteArrayInputStream("body".toByteArray()) }, { 4 })
        body.writeTo(ByteArrayOutputStream())
        body.writeTo(ByteArrayOutputStream())
    }

    @Test
    fun bodyToByteArrayLoadsItIntoMemory() {
        val value = "String Body ${Math.random()}"
        val body = DefaultBody.from({ ByteArrayInputStream(value.toByteArray()) }, { value.length.toLong() })
        body.toByteArray()

        val output = ByteArrayOutputStream(value.length)
        body.writeTo(output)
        assertThat(output.toString(), equalTo(value))
    }

    @Test
    fun requestWithBodyIsPrintableAfterConsumption() {
        val value = { ByteArrayInputStream("String Body ${Math.random()}".toByteArray()) }

        DefaultRequest(Method.POST, URL("https://test.fuel.com/"))
            .body(value)
            .apply {
                val output = ByteArrayOutputStream()
                body.writeTo(output)

                assertThat(this.toString(), containsString("(consumed)"))
            }
    }
}