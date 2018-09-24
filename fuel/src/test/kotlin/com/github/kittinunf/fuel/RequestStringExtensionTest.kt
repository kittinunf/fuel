package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.File
import java.net.HttpURLConnection
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RequestStringExtensionTest : MockHttpTestCase() {
    init {
        FuelManager.instance.baseHeaders = mapOf("foo" to "bar")
        FuelManager.instance.baseParams = listOf("key" to "value")
    }

    @Test
    fun httpGet() {
        mockChain(
            request = mockRequest().withMethod(Method.GET.value).withPath("/http-get"),
            response = mockReflect()
        )

        val (request, response, result) = mockPath("http-get").httpGet().responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpPost() {
        mockChain(
            request = mockRequest().withMethod(Method.POST.value).withPath("/http-post"),
            response = mockReflect()
        )

        val (request, response, result) = mockPath("http-post").httpPost().responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpPut() {
        mockChain(
            request = mockRequest().withMethod(Method.PUT.value).withPath("/http-put"),
            response = mockReflect()
        )

        val (request, response, result) = mockPath("http-put").httpPut().responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpPatch() {
        mockChain(
            request = mockRequest().withMethod(Method.PATCH.value).withPath("/http-patch"),
            response = mockReflect()
        )

        mockChain(
            request = mockRequest().withMethod(Method.POST.value).withHeader("X-HTTP-Method-Override", Method.PATCH.value).withPath("/http-patch"),
            response = mockReflect()
        )

        val (request, response, result) = mockPath("http-patch").httpPatch().responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpDelete() {
        mockChain(
            request = mockRequest().withMethod(Method.DELETE.value).withPath("/http-delete"),
            response = mockReflect()
        )

        val (request, response, result) = mockPath("http-delete").httpDelete().responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpDownload() {
        mockChain(
            request = mockRequest().withMethod(Method.GET.value).withPath("/http-download"),
            response = mockReflect()
        )

        val (request, response, result) = mockPath("http-download").httpDownload().destination { _, _ ->
            File.createTempFile("http-download.dl", null)
        }.responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpUploadWithPut() {
        mockChain(
            request = mockRequest().withMethod(Method.PUT.value).withPath("/http-upload"),
            response = mockReflect()
        )

        val (request, response, result) = mockPath("http-upload").httpUpload(Method.PUT).source { _, _ ->
            val dir = System.getProperty("user.dir")
            val currentDir = File(dir, "src/test/assets")
            File(currentDir, "lorem_ipsum_long.tmp")
        }.responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpUploadWithPost() {
        mockChain(
            request = mockRequest().withMethod(Method.POST.value).withPath("/http-upload"),
            response = mockReflect()
        )

        val (request, response, result) = mockPath("http-upload").httpUpload().source { _, _ ->
            val dir = System.getProperty("user.dir")
            val currentDir = File(dir, "src/test/assets")
            File(currentDir, "lorem_ipsum_long.tmp")
        }.responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpHead() {
        mockChain(
            request = mockRequest().withMethod(Method.HEAD.value).withPath("/http-head"),
            response = mockResponse().withStatusCode(HttpURLConnection.HTTP_OK)
        )

        val (request, response, result) = mockPath("http-head").httpHead().responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }
}