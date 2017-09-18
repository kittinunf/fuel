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
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RequestAndroidSyncTest : BaseTestCase() {

    init {
        FuelManager.instance.apply {
            basePath = "https://httpbin.org"
            baseHeaders = mapOf("foo" to "bar")
            baseParams = listOf("key" to "value")
        }
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

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
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

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
    }

}