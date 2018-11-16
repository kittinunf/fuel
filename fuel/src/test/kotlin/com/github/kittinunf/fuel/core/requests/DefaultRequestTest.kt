package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.net.URL

class DefaultRequestTest {

    @Test
    fun requestToStringIncludesMethod() {
        val request = DefaultRequest(
                Method.POST,
                url = URL("http://httpbin.org/post"),
                headers = Headers.from("Content-Type" to "text/html"),
                parameters = listOf("foo" to "xxx")
        ).body("it's a body")

        assertEquals(request.toString(), "--> POST http://httpbin.org/post\n" +
                "Body : it's a body\n" +
                "Headers : (1)\n" +
                "Content-Type : text/html\n")
    }
}
