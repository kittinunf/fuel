package com.github.kittinunf.fuel.json

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.ResponseHandler
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.test.MockHelper
import org.hamcrest.CoreMatchers.isA
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class FuelJsonTest {

    private lateinit var mock: MockHelper

    @Before
    fun setupFuelManager() {
        FuelManager.instance.apply {
            baseHeaders = mapOf("foo" to "bar")
            baseParams = listOf("key" to "value")
        }
        this.mock = MockHelper().apply { setup() }
    }

    @After
    fun resetFuelManager() {
        FuelManager.instance.reset()
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
        assertThat(data as FuelJson, isA(FuelJson::class.java))
        assertThat(data.obj(), isA(JSONObject::class.java))

        assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
    }

    @Test
    fun httpSyncRequestJsonWithHandlerTest() {
        mock.chain(
            request = mock.request().withPath("/get"),
            response = mock.reflect()
        )

        mock.path("get").httpGet(listOf("hello" to "world")).responseJson(object : ResponseHandler<FuelJson> {
            override fun success(request: Request, response: Response, value: FuelJson) {
                assertThat(value.obj(), isA(JSONObject::class.java))
                assertThat(response.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
            }

            override fun failure(request: Request, response: Response, error: FuelError) {
                fail("Expected request to succeed, actual $error")
            }
        })
    }
}
