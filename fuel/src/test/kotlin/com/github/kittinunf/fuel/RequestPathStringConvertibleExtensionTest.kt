package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FileDataPart
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.test.MockHttpTestCase
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.io.File
import java.net.HttpURLConnection

class RequestPathStringConvertibleExtensionTest : MockHttpTestCase() {
    class PathStringConvertibleImpl(url: String) : RequestFactory.PathStringConvertible {
        override val path = url
    }

    @Test
    fun httpGetRequestWithSharedInstance() {
        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/http-get"),
            response = mock.reflect()
        )

        val (request, response, result) = PathStringConvertibleImpl(mock.path("http-get"))
            .httpGet()
            .responseString()

        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, equalTo(statusCode))
    }

    @Test
    fun httpPostRequestWithSharedInstance() {
        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/http-post"),
            response = mock.reflect()
        )

        val (request, response, result) = PathStringConvertibleImpl(mock.path("http-post"))
            .httpPost()
            .responseString()

        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, equalTo(statusCode))

        assertThat(data, containsString("http-post"))
    }

    @Test
    fun httpPutRequestWithSharedInstance() {
        mock.chain(
            request = mock.request().withMethod(Method.PUT.value).withPath("/http-put"),
            response = mock.reflect()
        )

        val (request, response, result) = PathStringConvertibleImpl(mock.path("http-put"))
            .httpPut()
            .responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, equalTo(statusCode))

        assertThat(data, containsString("http-put"))
    }

    @Test
    fun httpPatchRequestWithSharedInstance() {
        mock.chain(
            request = mock.request().withMethod(Method.PATCH.value).withPath("/http-patch"),
            response = mock.reflect()
        )

        mock.chain(
            request = mock.request()
                .withMethod(Method.POST.value)
                .withHeader("X-HTTP-Method-Override", Method.PATCH.value)
                .withPath("/http-patch"),
            response = mock.reflect()
        )

        val (request, response, result) = PathStringConvertibleImpl(mock.path("http-patch"))
            .httpPatch()
            .responseString()

        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, equalTo(statusCode))

        assertThat(data, containsString("http-patch"))
    }

    @Test
    fun httpDeleteRequestWithSharedInstance() {
        mock.chain(
            request = mock.request().withMethod(Method.DELETE.value).withPath("/http-delete"),
            response = mock.reflect()
        )

        val (request, response, result) = PathStringConvertibleImpl(mock.path("http-delete"))
            .httpDelete()
            .responseString()

        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, equalTo(statusCode))

        assertThat(data, containsString("http-delete"))
    }

    @Test
    fun httpUploadRequestWithSharedInstance() {
        mock.chain(
            request = mock.request().withMethod(Method.POST.value).withPath("/http-upload"),
            response = mock.reflect()
        )

        val (request, response, result) = PathStringConvertibleImpl(mock.path("http-upload"))
            .httpUpload()
            .add { FileDataPart.from(File(System.getProperty("user.dir"), "src/test/assets").absolutePath, "lorem_ipsum_long.tmp") }
            .responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, equalTo(statusCode))
    }

    @Test
    fun httpDownloadRequestWithSharedInstance() {
        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/http-download"),
            response = mock.reflect()
        )

        val (request, response, result) = PathStringConvertibleImpl(mock.path("http-download"))
            .httpDownload()
            .fileDestination { _, _ -> File.createTempFile("123456", null) }
            .responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, equalTo(statusCode))
    }
}
