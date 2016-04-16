package com.github.kittinunf.fuel.android

import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.core.Manager
import com.github.kittinunf.fuel.httpGet
import org.hamcrest.CoreMatchers.*
import org.json.JSONObject
import org.junit.Assert.assertThat
import org.junit.Test
import java.net.HttpURLConnection
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RequestAndroidSyncTest : BaseTestCase() {

    init {
        Manager.instance.basePath = "https://httpbin.org"
        Manager.instance.baseHeaders = mapOf("foo" to "bar")
        Manager.instance.baseParams = listOf("key" to "value")
    }

    @Test
    fun httpSyncRequestTest() {
        val (request, response, result) = "/get".httpGet(listOf("hello" to "world")).responseJson()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())
        assertThat(data as JSONObject, isA(JSONObject::class.java))

        assertThat(response.httpStatusCode, isEqualTo(HttpURLConnection.HTTP_OK))
    }

}