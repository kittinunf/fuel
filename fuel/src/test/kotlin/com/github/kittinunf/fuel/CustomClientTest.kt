package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.File
import java.nio.charset.Charset

class CustomClientTest : BaseTestCase() {

    val manager: FuelManager by lazy {
        val dir = System.getProperty("user.dir")
        val currentDir = File(dir, "src/test/assets")
        val mockJson = File(currentDir, "mock.json")

        FuelManager().apply {
            client = object : Client {
                override fun executeRequest(request: Request): Response {
                    return Response().apply {
                        dataStream = mockJson.inputStream()
                        httpStatusCode = 200
                        request.client = client
                    }
                }
            }
        }
    }

    @Test
    fun httpRequestWithMockResponse() {
        val (request, response, data) =
                manager.request(Method.GET, "http://foo.bar").response()
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(data.get(), notNullValue())
        assertThat(data.get().toString(Charset.defaultCharset()), containsString("key"))
        assertThat(data.get().toString(Charset.defaultCharset()), containsString("value"))
    }
}