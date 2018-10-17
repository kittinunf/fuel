package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.*
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.Reader
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RequestObjectTest : MockHttpTestCase() {

    data class ReflectMockModel(var userAgent: String = "") {
        class Deserializer: ResponseDeserializable<ReflectMockModel> {
            override fun deserialize(content: String): ReflectMockModel = ReflectMockModel(content)
        }

        class MalformedDeserializer: ResponseDeserializable<ReflectMockModel> {
            override fun deserialize(reader: Reader): ReflectMockModel = throw IllegalStateException("Malformed data")
        }
    }

    @Test
    fun httpRequestObjectUserAgentValidTest() {
        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/user-agent"),
            response = mock.reflect()
        )

        val (request, response, result) = Fuel.get(mock.path("user-agent")).responseObject(ReflectMockModel.Deserializer())
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(data as ReflectMockModel, isA(ReflectMockModel::class.java))
        assertThat(data.userAgent, isEqualTo(not("")))
    }

    @Test
    fun httpRequestObjectUserAgentInvalidTest() {
        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/user-agent"),
            response = mock.reflect()
        )

        val (request, response, result) = Fuel.get(mock.path("user-agent")).responseObject(ReflectMockModel.MalformedDeserializer())
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, notNullValue())
        assertThat(data, nullValue())

        assertThat(error?.exception as IllegalStateException, isA(IllegalStateException::class.java))
        assertThat(error.exception.message, equalTo("Malformed data"))
    }

    @Test
    fun httpRequestObjectUserAgentInvalidSync() {
        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/user-agent"),
            response = mock.reflect()
        )

        val (request, response, result) = Fuel.get(mock.path("user-agent")).responseObject(ReflectMockModel.MalformedDeserializer())
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, notNullValue())
        assertThat(data, nullValue())

        assertThat(error?.exception as IllegalStateException, isA(IllegalStateException::class.java))
        assertThat(error.exception.message, equalTo("Malformed data"))
    }

}
