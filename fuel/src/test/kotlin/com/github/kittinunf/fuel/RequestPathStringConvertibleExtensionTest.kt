package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.Method
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.File
import java.net.HttpURLConnection
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RequestPathStringConvertibleExtensionTest : MockHttpTestCase() {
    class PathStringConvertibleImpl(url: String) : Fuel.PathStringConvertible {
        override val path = url
    }


    @Test
    fun httpGetRequestWithSharedInstance() {
        mockChain(
            request = mockRequest().withMethod(Method.GET.value).withPath("/http-get"),
            response = mockReflect()
        )

        val (request, response, result) = PathStringConvertibleImpl(mockPath("http-get")).httpGet().responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpPostRequestWithSharedInstance() {
        mockChain(
            request = mockRequest().withMethod(Method.POST.value).withPath("/http-post"),
            response = mockReflect()
        )

        val (request, response, result) = PathStringConvertibleImpl(mockPath("http-post")).httpPost().responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        assertThat(data, containsString("http-post"))
    }

    @Test
    fun httpPutRequestWithSharedInstance() {
        mockChain(
            request = mockRequest().withMethod(Method.PUT.value).withPath("/http-put"),
            response = mockReflect()
        )

        val (request, response, result) = PathStringConvertibleImpl(mockPath("http-put")).httpPut().responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        assertThat(data, containsString("http-put"))
    }

    @Test
    fun httpPatchRequestWithSharedInstance() {
        mockChain(
            request = mockRequest().withMethod(Method.PATCH.value).withPath("/http-patch"),
            response = mockReflect()
        )

        mockChain(
            request = mockRequest().withMethod(Method.POST.value).withHeader("X-HTTP-Method-Override", Method.PATCH.value).withPath("/http-patch"),
            response = mockReflect()
        )

        val (request, response, result) = PathStringConvertibleImpl(mockPath("http-patch")).httpPatch().responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        assertThat(data, containsString("http-patch"))
    }

    @Test
    fun httpDeleteRequestWithSharedInstance() {
        mockChain(
            request = mockRequest().withMethod(Method.DELETE.value).withPath("/http-delete"),
            response = mockReflect()
        )

        val (request, response, result) = PathStringConvertibleImpl(mockPath("http-delete")).httpDelete().responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        assertThat(data, containsString("http-delete"))
    }

    @Test
    fun httpUploadRequestWithSharedInstance() {
        mockChain(
            request = mockRequest().withMethod(Method.POST.value).withPath("/http-upload"),
            response = mockReflect()
        )

        val (request, response, result) = PathStringConvertibleImpl(mockPath("http-upload")).httpUpload().source { _, _ ->
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
    fun httpDownloadRequestWithSharedInstance() {
        mockChain(
            request = mockRequest().withMethod(Method.GET.value).withPath("/http-download"),
            response = mockReflect()
        )

        val (request, response, result) = PathStringConvertibleImpl(mockPath("http-download")).httpDownload().destination { _, _ ->
            File.createTempFile("123456", null)
        }.responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

}