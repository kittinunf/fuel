package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.*
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.File
import java.nio.charset.Charset

class CustomClientTest : BaseTestCase() {
    private val manager: FuelManager by lazy {
        val dir = System.getProperty("user.dir")
        val currentDir = File(dir, "src/test/assets")
        val mockJson = File(currentDir, "mock.json")

        FuelManager().apply {
            client = object : Client {
                override fun executeRequest(request: Request): Response = Response(
                        url = request.url,
                        dataStream = mockJson.inputStream(),
                        statusCode = 200)
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