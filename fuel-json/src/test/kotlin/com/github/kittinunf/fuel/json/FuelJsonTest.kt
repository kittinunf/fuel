package com.github.kittinunf.fuel.json

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.ResponseHandler
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.test.MockHelper
import org.hamcrest.CoreMatchers.isA
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection
import java.util.concurrent.CountDownLatch
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

        val (request, response, result) =
                mock.path("get").httpGet(listOf("hello" to "world")).responseJson()
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
    fun httpSyncRequestJsonArrayTest() {
        mock.chain(
            request = mock.request().withPath("/gets"),
            response = mock.response().withBody("[ " +
                    "{ \"id\": 1, \"foo\": \"foo 1\", \"bar\": null }, " +
                    "{ \"id\": 2, \"foo\": \"foo 2\", \"bar\": 32 }, " +
                    " ]").withStatusCode(HttpURLConnection.HTTP_OK)
        )

        val (request, response, result) =
                mock.path("gets").httpGet().responseJson()
        val (data, error) = result

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())
        assertThat(data as FuelJson, isA(FuelJson::class.java))
        assertThat(data.array(), isA(JSONArray::class.java))
        assertThat(data.array().length(), isEqualTo(2))

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

    @Test
    fun httpASyncRequestJsonTest() {
        val lock = CountDownLatch(1)

        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        Fuel.get(mock.path("user-agent")).responseJson { req, res, result ->
            val (d, e) = result
            data = d
            error = e

            request = req
            response = res

            lock.countDown()
        }

        lock.await()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())
        assertThat(data as FuelJson, isA(FuelJson::class.java))
        assertThat((data as FuelJson).obj(), isA(JSONObject::class.java))

        assertThat(response!!.statusCode, isEqualTo(HttpURLConnection.HTTP_OK))
    }

    @Test
    fun httpASyncRequestJsonInvalidTest() {
        val lock = CountDownLatch(1)

        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        mock.chain(
            request = mock.request().withPath("/404"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        Fuel.get(mock.path("404")).responseString { req, res, result ->
            val (d, e) = result
            data = d
            error = e

            request = req
            response = res

            lock.countDown()
        }

        lock.await()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, notNullValue())
        assertThat(data, nullValue())

        val statusCode = HttpURLConnection.HTTP_NOT_FOUND
        assertThat(response?.statusCode, isEqualTo(statusCode))
    }
}
