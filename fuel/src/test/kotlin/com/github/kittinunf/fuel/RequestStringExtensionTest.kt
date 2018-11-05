package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.test.MockHttpTestCase
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
        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/http-get"),
            response = mock.reflect()
        )

        val (request, response, result) = mock.path("http-get").httpGet().responseString()
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
        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/http-post"),
            response = mock.reflect()
        )

        val (request, response, result) = mock.path("http-post").httpPost().responseString()
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
        mock.chain(
            request = mock.request().withMethod(Method.PUT.value).withPath("/http-put"),
            response = mock.reflect()
        )

        val (request, response, result) = mock.path("http-put").httpPut().responseString()
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
        mock.chain(
            request = mock.request().withMethod(Method.PATCH.value).withPath("/http-patch"),
            response = mock.reflect()
        )

        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withHeader("X-HTTP-Method-Override", Method.PATCH.value).withPath("/http-patch"),
            response = mock.reflect()
        )

        val (request, response, result) = mock.path("http-patch").httpPatch().responseString()
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
        mock.chain(
            request = mock.request().withMethod(Method.DELETE.value).withPath("/http-delete"),
            response = mock.reflect()
        )

        val (request, response, result) = mock.path("http-delete").httpDelete().responseString()
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
        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/http-download"),
            response = mock.reflect()
        )

        val (request, response, result) = mock.path("http-download").httpDownload().destination { _, _ ->
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
        mock.chain(
            request = mock.request().withMethod(Method.PUT.value).withPath("/http-upload"),
            response = mock.reflect()
        )

        val (request, response, result) = mock.path("http-upload").httpUpload(Method.PUT).source { _, _ ->
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
        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/http-upload"),
            response = mock.reflect()
        )

        val (request, response, result) = mock.path("http-upload").httpUpload().source { _, _ ->
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
        mock.chain(
            request = mock.request().withMethod(Method.HEAD.value).withPath("/http-head"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_OK)
        )

        val (request, response, result) = mock.path("http-head").httpHead().responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }
}