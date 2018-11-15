package com.github.kittinunf.fuel.json

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpGet
import org.hamcrest.CoreMatchers.isA
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.json.JSONObject
import org.junit.Assert.assertThat
import org.junit.Test
import java.net.HttpURLConnection
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class FuelJsonTest {

	init {
        Fuel.testMode {
            timeout = 15000
        }
        FuelManager.instance.apply {
            baseHeaders = mapOf("foo" to "bar")
            baseParams = listOf("key" to "value")
        }
    }

    private lateinit var mock: MockHelper

    @Before
    fun setup() {
        this.mock = MockHelper()
        this.mock.setup()
    }

    @After
    fun tearDown() {
        this.mock.tearDown()
    }

    @Test
    fun httpSyncRequestStringTest() {
        mock.chain(
            request = mock.request().withPath("/get"),
            response = mock.reflect()
        )

        val (request, response, result) = mock.path("get").httpGet(listOf("hello" to "world")).responseString()
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
        mock.chain(
            request = mock.request().withPath("/get"),
            response = mock.reflect()
        )

        val (request, response, result) = mock.path("get").httpGet(listOf("hello" to "world")).responseJson()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())
        assertThat(data as Json, isA(FuelJson::class.java))
        assertThat(data.obj(), isA(JSONObject::class.java))

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
    }
}