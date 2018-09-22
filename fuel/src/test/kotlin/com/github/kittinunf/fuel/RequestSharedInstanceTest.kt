package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.Encoding
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.File
import java.net.HttpURLConnection
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RequestSharedInstanceTest : MockHttpTestCase() {

    class PathStringConvertibleImpl(url: String) : Fuel.PathStringConvertible {
        override val path = url
    }

    class RequestConvertibleImpl(val method: Method, private val url: String) : Fuel.RequestConvertible {
        override val request = createRequest()

        private fun createRequest(): Request {
            val encoder = Encoding(
                    httpMethod = method,
                    urlString = url,
                    parameters = listOf("foo" to "bar")
            )
            return encoder.request
        }
    }

    init {
        FuelManager.instance.baseHeaders = mapOf("foo" to "bar")
        FuelManager.instance.baseParams = listOf("key" to "value")
    }

    @Test
    fun httpGetRequestWithSharedInstance() {
        mockChain(
            request = mockRequest().withMethod(Method.GET.value).withPath("/Fuel/get"),
            response = mockReflect()
        )
        val (request, response, result) = Fuel.get(mockPath("Fuel/get")).responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        val string = data as String

        assertThat(string.toLowerCase(), containsString("foo"))
        assertThat(string.toLowerCase(), containsString("bar"))
        assertThat(string.toLowerCase(), containsString("key"))
        assertThat(string.toLowerCase(), containsString("value"))

        assertThat(string, containsString("Fuel/get"))
    }

    @Test
    fun httpPostRequestWithSharedInstance() {
        mockChain(
            request = mockRequest().withMethod(Method.POST.value).withPath("/Fuel/post"),
            response = mockReflect()
        )
        val (request, response, result) = Fuel.post(mockPath("Fuel/post")).responseString()
        val (data, error) = result

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        assertThat(string.toLowerCase(), containsString("foo"))
        assertThat(string.toLowerCase(), containsString("bar"))
        assertThat(string.toLowerCase(), containsString("key"))
        assertThat(string.toLowerCase(), containsString("value"))

        assertThat(string, containsString("Fuel/post"))
    }

    @Test
    fun httpPutRequestWithSharedInstance() {
        mockChain(
            request = mockRequest().withMethod(Method.PUT.value).withPath("/Fuel/put"),
            response = mockReflect()
        )
        val (request, response, result) = Fuel.put(mockPath("Fuel/put")).responseString()
        val (data, error) = result

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        assertThat(string.toLowerCase(), containsString("foo"))
        assertThat(string.toLowerCase(), containsString("bar"))
        assertThat(string.toLowerCase(), containsString("key"))
        assertThat(string.toLowerCase(), containsString("value"))

        assertThat(string, containsString("Fuel/put"))
    }

    @Test
    fun httpDeleteRequestWithSharedInstance() {
        mockChain(
            request = mockRequest().withMethod(Method.DELETE.value).withPath("/Fuel/delete"),
            response = mockReflect()
        )
        val (request, response, result) = Fuel.delete(mockPath("Fuel/delete")).responseString()
        val (data, error) = result

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        assertThat(string.toLowerCase(), containsString("foo"))
        assertThat(string.toLowerCase(), containsString("bar"))
        assertThat(string.toLowerCase(), containsString("key"))
        assertThat(string.toLowerCase(), containsString("value"))

        assertThat(string, containsString("Fuel/delete"))
    }

    @Test
    fun httpGetRequestWithPathStringConvertibleAndSharedInstance() {
        mockChain(
            request = mockRequest().withMethod(Method.GET.value).withPath("/Fuel/get"),
            response = mockReflect()
        )
        val (request, response, result) = Fuel.get(PathStringConvertibleImpl(mockPath("Fuel/get"))).responseString()
        val (data, error) = result

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        assertThat(string, containsString("Fuel/get"))
    }

    @Test
    fun httpPostRequestWithPathStringConvertibleAndSharedInstance() {
        mockChain(
            request = mockRequest().withMethod(Method.POST.value).withPath("/Fuel/post"),
            response = mockReflect()
        )
        val (request, response, result) = Fuel.post(PathStringConvertibleImpl(mockPath("Fuel/post"))).responseString()
        val (data, error) = result

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        assertThat(string, containsString("Fuel/post"))
    }

    @Test
    fun httpPutRequestWithPathStringConvertibleAndSharedInstance() {
        mockChain(
            request = mockRequest().withMethod(Method.PUT.value).withPath("/Fuel/put"),
            response = mockReflect()
        )
        val (request, response, result) = Fuel.put(PathStringConvertibleImpl(mockPath("Fuel/put"))).responseString()
        val (data, error) = result

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        assertThat(string, containsString("Fuel/put"))
    }

    @Test
    fun httpDeleteRequestWithPathStringConvertibleAndSharedInstance() {
        mockChain(
            request = mockRequest().withMethod(Method.DELETE.value).withPath("/Fuel/delete"),
            response = mockReflect()
        )
        val (request, response, result) = Fuel.delete(PathStringConvertibleImpl(mockPath("Fuel/delete"))).responseString()
        val (data, error) = result

        val string = data as String

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        assertThat(string, containsString("Fuel/delete"))
    }

    @Test
    fun httpGetRequestWithRequestConvertibleAndSharedInstance() {
        mockChain(
            request = mockRequest().withMethod(Method.GET.value).withPath("/Fuel/get"),
            response = mockReflect()
        )
        val (request, response, result) = Fuel.request(RequestConvertibleImpl(Method.GET, mockPath("Fuel/get"))).responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpPostRequestWithRequestConvertibleAndSharedInstance() {
        mockChain(
            request = mockRequest().withMethod(Method.POST.value).withPath("/Fuel/post"),
            response = mockReflect()
        )
        val (request, response, result) = Fuel.request(RequestConvertibleImpl(Method.POST, mockPath("Fuel/post"))).responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpPutRequestWithRequestConvertibleAndSharedInstance() {
        mockChain(
            request = mockRequest().withMethod(Method.PUT.value).withPath("/Fuel/put"),
            response = mockReflect()
        )
        val (request, response, result) = Fuel.request(RequestConvertibleImpl(Method.PUT, mockPath("Fuel/put"))).responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpDeleteRequestWithRequestConvertibleAndSharedInstance() {
        mockChain(
            request = mockRequest().withMethod(Method.DELETE.value).withPath("/Fuel/delete"),
            response = mockReflect()
        )
        val (request, response, result) = Fuel.request(RequestConvertibleImpl(Method.DELETE, mockPath("Fuel/delete"))).responseString()
        val (data, error) = result
        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpUploadWithProgressValidCase() {
        mockChain(
            request = mockRequest().withMethod(Method.POST.value).withPath("/Fuel/upload"),
            response = mockReflect()
        )

        var read = -1L
        var total = -1L

        val (request, response, result) = Fuel.upload(mockPath("Fuel/upload")).source { _, _ ->
            val dir = System.getProperty("user.dir")
            File(dir, "src/test/assets/lorem_ipsum_long.tmp")
        }.progress { readBytes, totalBytes ->
            read = readBytes
            total = totalBytes
            println("read: $read, total: $total")
        }.responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        assertThat(read == total && read != -1L && total != -1L, isEqualTo(true))
    }

    @Test
    fun httpDownloadWithProgressValidCase() {
        mockChain(
            request = mockRequest().withMethod(Method.GET.value).withPath("/Fuel/download"),
            response = mockReflect()
        )

        var read = -1L
        var total = -1L

        val (request, response, result) = Fuel.download(mockPath("Fuel/download"))
            .destination { _, _ ->
                File.createTempFile("download.dl", null)
            }.progress { readBytes, totalBytes ->
                read = readBytes
                total = totalBytes
            }.responseString()
        val (data, error) = result


        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        assertThat(read, isEqualTo(total))
    }

}
