package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.*
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Test
import javax.net.ssl.HttpsURLConnection
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RedirectionTest : BaseTestCase() {
    private val manager: FuelManager by lazy {
        FuelManager().apply {
            basePath = "https://httpstat.us"
        }
    }

    @Test
    fun httpRedirection() {
        var request: Request? = null
        var response: Response? = null
        var redirectLocation: String? = null
        var data: Any? = null
        var error: FuelError? = null

        manager.request(Method.GET, "/303").response { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            data = d
            error = err
            
            redirectLocation = response.httpResponseHeaders["Location"]
        }

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())
        assertThat(redirectLocation, "https://httpstat.us")

        val statusCode = HttpsURLConnection.HTTP_SEE_OTHER
        assertThat(response?.statusCode, isEqualTo(statusCode))
    }

}
