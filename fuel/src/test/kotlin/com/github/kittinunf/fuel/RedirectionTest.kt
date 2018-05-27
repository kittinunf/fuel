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
        var data: Any? = null
        var error: FuelError? = null

        manager.request(Method.GET, "/303").response { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            data = d
            error = err
        }

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        //Even though the URL gives a 303, we should receive a 200
        //response, as Fuel will handle the redirect for the user
        val statusCode = HttpsURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))
    }

}
