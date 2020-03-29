package com.github.kittinunf.fuel

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.requests.DefaultRequest
import com.github.kittinunf.fuel.jackson.objectBody
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.net.URL

class ObjectBodyTest {
    @Test
    fun setsBodyCorrectly() {
        val expectedBody = "{\"foo\":42,\"bar\":\"foo bar\",\"fooBar\":\"foo bar\"}"
        val bodyObject = FakeObject()

        val request = DefaultRequest(Method.POST, URL("https://test.fuel.com/body"))
            .objectBody(bodyObject)

        assertThat(expectedBody, equalTo(String(request.body.toByteArray())))
    }

    @Test
    fun setsContentTypeCorrectly() {
        val bodyObject = listOf(
            42,
            mapOf("foo" to "bar")
        )

        val request = DefaultRequest(Method.POST, URL("https://test.fuel.com/body"))
            .objectBody(bodyObject)

        assertThat(request[Headers.CONTENT_TYPE].lastOrNull(), equalTo("application/json"))
    }

    @Test
    fun setsBodyCorrectlyWithCustomMapper() {
        val mapper = createCustomMapper()
        val expectedBody = "{\"foo\":42,\"bar\":\"foo bar\",\"foo_bar\":\"foo bar\"}"
        val bodyObject = FakeObject()

        val request = DefaultRequest(Method.POST, URL("https://test.fuel.com/body"))
            .objectBody(bodyObject, mapper = mapper)

        assertThat(expectedBody, equalTo(String(request.body.toByteArray())))
    }

    private fun createCustomMapper(): ObjectMapper {
        val mapper = ObjectMapper().registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        mapper.propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE

        return mapper
    }
}

data class FakeObject(
    val foo: Int = 42,
    val bar: String = "foo bar",
    val fooBar: String = "foo bar"
)
