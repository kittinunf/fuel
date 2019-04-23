package com.github.kittinunf.fuel.android

import android.util.JsonReader
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.core.ResponseHandler
import org.hamcrest.CoreMatchers.isA
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.io.StringReader
import java.net.HttpURLConnection
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RequestAndroidAsyncTest : BaseTestCase() {

    @Before
    fun setupFuelManager() {
        FuelManager.instance.apply {
            baseHeaders = mapOf("foo" to "bar")
            baseParams = listOf("key" to "value")
            callbackExecutor = Executor(Runnable::run)
        }
    }

    @After
    fun resetFuelManager() {
        FuelManager.instance.reset()
    }

    // Model
    data class HttpBinHeadersModel(var headers: Map<String, List<String>> = mutableMapOf())

    // Deserializer
    class HttpBinHeadersDeserializer : ResponseDeserializable<HttpBinHeadersModel> {

        override fun deserialize(content: String): HttpBinHeadersModel {
            val model = HttpBinHeadersModel()
            val reader = JsonReader(StringReader(content))
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "headers" -> model.headers = deserializeHeaders(reader)
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
            return model
        }

        private fun deserializeHeaders(reader: JsonReader): Map<String, List<String>> {
            val result = hashMapOf<String, List<String>>()
            reader.beginObject()
            while (reader.hasNext()) {
                val name = reader.nextName()
                val values = mutableListOf<String>()
                reader.beginArray()
                while (reader.hasNext()) {
                    values.add(reader.nextString())
                }
                reader.endArray()
                result[name] = values
            }
            reader.endObject()
            return result
        }
    }

    @Before
    fun setUp() {
        lock = CountDownLatch(1)
    }

    @Test
    fun httpGetRequestString() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        Fuel.get(mock.path("user-agent")).responseString { req, res, result ->
            val (d, e) = result
            data = d
            error = e

            request = req
            response = res

            lock.countDown()
        }

        await()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())
        assertThat(data as String, isA(String::class.java))

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpGetRequestJsonValid() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        Fuel.get(mock.path("user-agent")).responseString { req, res, result ->
            val (d, e) = result
            data = d
            error = e

            request = req
            response = res

            lock.countDown()
        }

        await()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())

        assertThat(data as String, isA(String::class.java))

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpGetRequestJsonHandlerValid() {
        var req: Request? = null
        var res: Response? = null
        var data: Any? = null
        var err: FuelError? = null

        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        Fuel.get(mock.path("user-agent")).responseString(object : ResponseHandler<String> {
            override fun success(request: Request, response: Response, value: String) {
                req = request
                res = response
                data = value

                lock.countDown()
            }

            override fun failure(request: Request, response: Response, error: FuelError) {
                err = error

                lock.countDown()
            }
        })

        await()

        assertThat(req, notNullValue())
        assertThat(res, notNullValue())
        assertThat(err, nullValue())
        assertThat(data, notNullValue())
        assertThat(data as String, isA(String::class.java))

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(res?.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpGetRequestJsonInvalid() {
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

        await()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, notNullValue())
        assertThat(data, nullValue())

        val statusCode = HttpURLConnection.HTTP_NOT_FOUND
        assertThat(response?.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpGetRequestJsonHandlerInvalid() {
        var req: Request? = null
        var res: Response? = null
        var data: Any? = null
        var err: FuelError? = null

        mock.chain(
            request = mock.request().withPath("/404"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        Fuel.get(mock.path("404")).responseString(object : ResponseHandler<String> {
            override fun success(request: Request, response: Response, value: String) {
                data = value

                lock.countDown()
            }

            override fun failure(request: Request, response: Response, error: FuelError) {
                req = request
                res = response
                err = error

                lock.countDown()
            }
        })

        await()

        assertThat(req, notNullValue())
        assertThat(res, notNullValue())
        assertThat(err, notNullValue())
        assertThat(data, nullValue())

        val statusCode = HttpURLConnection.HTTP_NOT_FOUND
        assertThat(res?.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpGetRequestObject() {
        var request: Request? = null
        var response: Response? = null
        var data: HttpBinHeadersModel? = null
        var error: FuelError? = null

        mock.chain(
            request = mock.request().withPath("/headers"),
            response = mock.reflect()
        )

        Fuel.get(mock.path("headers")).responseObject(HttpBinHeadersDeserializer()) { req, res, result ->
            val (d, e) = result
            request = req
            response = res
            data = d
            error = e

            lock.countDown()
        }

        await()

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, nullValue())
        assertThat(data, notNullValue())
        assertThat(data!!.headers.isNotEmpty(), isEqualTo(true))
        assertThat(data!!.headers["foo"]?.firstOrNull(), isEqualTo("bar"))

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(response?.statusCode, isEqualTo(statusCode))
    }

    @Test
    fun httpGetRequestHandlerObject() {
        var req: Request? = null
        var res: Response? = null
        var data: HttpBinHeadersModel? = null
        var err: FuelError? = null

        mock.chain(
            request = mock.request().withPath("/headers"),
            response = mock.reflect()
        )

        Fuel.get(mock.path("headers")).responseObject(HttpBinHeadersDeserializer(), object : ResponseHandler<HttpBinHeadersModel> {

            override fun success(request: Request, response: Response, value: HttpBinHeadersModel) {
                req = request
                res = response
                data = value

                lock.countDown()
            }

            override fun failure(request: Request, response: Response, error: FuelError) {
                err = error

                lock.countDown()
            }
        })

        await()

        assertThat(req, notNullValue())
        assertThat(res, notNullValue())
        assertThat(err, nullValue())
        assertThat(data, notNullValue())
        assertThat(data!!.headers.isNotEmpty(), isEqualTo(true))
        assertThat(data!!.headers["foo"]?.firstOrNull(), isEqualTo("bar"))

        val statusCode = HttpURLConnection.HTTP_OK
        assertThat(res?.statusCode, isEqualTo(statusCode))
    }
}
