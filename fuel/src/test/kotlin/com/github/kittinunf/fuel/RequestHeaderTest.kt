package com.github.kittinunf.fuel


import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.FuelManager
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.*
import org.junit.Test
import java.net.HttpURLConnection
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RequestHeaderTest : MockHttpTestCase() {
    @Test
    fun httpRequestHeader() {
        val manager = FuelManager()

        mockChain(
            request = mockRequest().withMethod(Method.GET.value).withPath("/get"),
            response = mockReflect()
        )

        val headerKey = "Custom"
        val headerValue = "foobar"

        val (request, response, result) = manager.request(Method.GET, mockPath("get"))
                .header(headerKey to headerValue).response()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response.statusCode, isEqualTo(statusCode))

        val string = String(data as ByteArray)
        assertThat(string, containsString(headerKey))
        assertThat(string, containsString(headerValue))
    }

    @Test
    fun httpRequestHeaderWithDuplicateValuesForCookieKey() {
        val manager = FuelManager()

        mockChain(
            request = mockRequest().withMethod(Method.GET.value).withPath("/get"),
            response = mockReflect()
        )

        val (_, response, _) = manager.request(Method.GET, mockPath("get"))
                .header("foo" to "bar","a" to "b", "cookie" to "val1=x", "cookie" to "val2=y","cookie" to "val3=z", "cookie" to "val4=j")
                .responseString()

        assertEquals("val1=x; val2=y; val3=z; val4=j", response.headers["Cookie"]?.firstOrNull())
    }

    @Test
    fun multipleHeadersByTheSameKeyWillBeCorrectlyFormatted(){
        val manager = FuelManager()
        val request = manager.request(Method.GET, mockPath("get"))
                .header("foo" to "bar","a" to "b", "cookie" to "val1=x", "cookie" to "val2=y","cookie" to "val3=z", "cookie" to "val4=j")

        assertEquals("val1=x; val2=y; val3=z; val4=j",request.headers["cookie"] )
        assertEquals("bar",request.headers["foo"] )
        assertEquals("b",request.headers["a"] )
    }

    @Test
    fun multipleHeadersByTheSameKeyWillShowLastUsingMap(){
        val manager = FuelManager()
        val request = manager.request(Method.GET, mockPath("get"))
                .header(mapOf("cookie" to "val1=x", "cookie" to "val2=y"),false)

        assertEquals("val2=y",request.headers["cookie"] )
    }
}