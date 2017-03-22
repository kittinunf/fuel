package com.github.kittinunf.fuel.android

import com.github.kittinunf.fuel.android.core.Json
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpGet
import org.hamcrest.CoreMatchers.*
import org.json.JSONObject
import org.junit.Assert.assertThat
import org.junit.Test
import java.net.HttpURLConnection
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RequestAndroidSyncTest : BaseTestCase() {

    init {
        FuelManager.instance.basePath = "https://httpbin.org"
        FuelManager.instance.baseHeaders = mapOf("foo" to "bar")
        FuelManager.instance.baseParams = listOf("key" to "value")

        //configure SSLContext that accepts any cert, you should not do this in your app but this is in test ¯\_(ツ)_/¯

        val acceptsAllTrustManager = object : X509TrustManager {
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}

            override fun getAcceptedIssuers(): Array<X509Certificate>? = null

            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        }

        FuelManager.instance.socketFactory = {
            val context = SSLContext.getInstance("TLS")
            context.init(null, arrayOf<TrustManager>(acceptsAllTrustManager), SecureRandom())
            SSLContext.setDefault(context)
            context.socketFactory
        }()
    }

    @Test
    fun httpSyncRequestStringTest() {
        val (request, response, result) = "/get".httpGet(listOf("hello" to "world")).responseString()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())
        assertThat(data as String, isA(String::class.java))

        assertThat(response.httpStatusCode, isEqualTo(HttpURLConnection.HTTP_OK))
    }

    @Test
    fun httpSyncRequestJsonTest() {
        val (request, response, result) = "/get".httpGet(listOf("hello" to "world")).responseJson()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())
        assertThat(data as Json, isA(Json::class.java))
        assertThat(data.obj(), isA(JSONObject::class.java))

        assertThat(response.httpStatusCode, isEqualTo(HttpURLConnection.HTTP_OK))
    }

}