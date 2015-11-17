package com.github.kittinunf.fuel.android

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.core.*
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Created by Kittinun Vantasin on 11/8/15.
 */

public class RequestAndroidHandlerTest : BaseTestCase() {

    init {
        Manager.instance.basePath = "https://httpbin.org"
        Manager.instance.baseHeaders = mapOf("foo" to "bar")
        Manager.instance.baseParams = listOf("key" to "value")

        Manager.instance.callbackExecutor = object : Executor {
            override fun execute(command: Runnable) {
                command.run()
            }
        }

    }

    //Model
    data class HttpBinHeadersModel(var headers: Map<String, String> = hashMapOf())

    //Deserializer
    class HttpBinHeadersDeserializer : ResponseDeserializable<HttpBinHeadersModel> {

        override fun deserialize(content: String): HttpBinHeadersModel {
            val json = JSONObject(content)
            val headers = json.getJSONObject("headers")
            val results = headers.keys().asSequence().toMap({ it }, { headers.getString(it) })
            val model = HttpBinHeadersModel()
            model.headers = results
            return model
        }

    }

    @Before
    fun setUp() {
        lock = CountDownLatch(1)
    }

    @Test
    fun httpGetRequestJsonValid() {
        var req: Request? = null
        var res: Response? = null
        var data: Any? = null
        var err: FuelError? = null

        Fuel.get("/user-agent").responseJson { request, response, either ->
            val (e, d) = either
            data = d
            err = e

            req = request
            res = response

            lock.countDown()
        }

        await()

        assertNotNull(req, "request should not be null")
        assertNotNull(res, "response should not be null")
        assertNull(err, "error should be null")
        assertNotNull(data, "data should not be null")
        assertTrue(data is JSONObject, "data should be JSONObject type")
        assertTrue(res?.httpStatusCode == HttpURLConnection.HTTP_OK, "http status code should be ${HttpURLConnection.HTTP_OK}")
    }

    @Test
    fun httpGetRequestJsonHandlerValid() {
        var req: Request? = null
        var res: Response? = null
        var data: Any? = null
        var err: FuelError? = null

        Fuel.get("/user-agent").responseJson(object : Handler<JSONObject> {
            override fun success(request: Request, response: Response, value: JSONObject) {
                req = request
                res = response
                data = value

                lock.countDown()
            }

            override fun failure(request: Request, response: Response, error: FuelError) {
            }
        })

        await()

        assertNotNull(req, "request should not be null")
        assertNotNull(res, "response should not be null")
        assertNull(err, "error should be null")
        assertNotNull(data, "data should not be null")
        assertTrue(data is JSONObject, "data should be JSONObject type")
        assertTrue(res?.httpStatusCode == HttpURLConnection.HTTP_OK, "http status code should be ${HttpURLConnection.HTTP_OK}")
    }

    @Test
    fun httpGetRequestJsonInvalid() {
        var req: Request? = null
        var res: Response? = null
        var data: Any? = null
        var err: FuelError? = null

        Fuel.get("/404").responseJson { request, response, either ->
            val (e, d) = either
            data = d
            err = e

            req = request
            res = response

            lock.countDown()
        }

        await()

        assertNotNull(req, "request should not be null")
        assertNotNull(res, "response should not be null")
        assertNotNull(err, "error should not be null")
        assertNull(data, "data should be null")
        assertTrue(res?.httpStatusCode == HttpURLConnection.HTTP_NOT_FOUND, "http status code (${res?.httpStatusCode}) should be ${HttpURLConnection.HTTP_NOT_FOUND}")
    }

    @Test
    fun httpGetRequestJsonHandlerInvalid() {
        var req: Request? = null
        var res: Response? = null
        var data: Any? = null
        var err: FuelError? = null

        Fuel.get("/404").responseJson(object : Handler<JSONObject> {
            override fun success(request: Request, response: Response, value: JSONObject) {
            }

            override fun failure(request: Request, response: Response, error: FuelError) {
                req = request
                res = response
                err = error

                lock.countDown()
            }
        })

        await()

        assertNotNull(req, "request should not be null")
        assertNotNull(res, "response should not be null")
        assertNotNull(err, "error should not be null")
        assertNull(data, "data should be null")
        assertTrue(res?.httpStatusCode == HttpURLConnection.HTTP_NOT_FOUND, "http status code (${res?.httpStatusCode}) should be ${HttpURLConnection.HTTP_NOT_FOUND}")
    }

    @Test
    fun httpGetRequestObject() {
        var req: Request? = null
        var res: Response? = null
        var data: Any? = null
        var err: FuelError? = null

        Fuel.get("/headers").responseObject(HttpBinHeadersDeserializer()) { request, response, either ->
            val (e, d) = either
            req = request
            res = response
            data = d
            err = e

            lock.countDown()
        }

        await()

        assertNotNull(req, "request should not be null")
        assertNotNull(res, "response should not be null")
        assertNull(err, "error should be null")
        assertNotNull(data, "data should not be null")
        assertTrue(data is HttpBinHeadersModel, "data should be HttpBinHeadersModel type")
        assertFalse((data as HttpBinHeadersModel).headers.isEmpty(), "model must properly be serialized")
    }

    @Test
    fun httpGetRequestHandlerObject() {
        var req: Request? = null
        var res: Response? = null
        var data: Any? = null
        var err: FuelError? = null

        Fuel.get("/headers").responseObject(HttpBinHeadersDeserializer(), object : Handler<HttpBinHeadersModel> {

            override fun success(request: Request, response: Response, value: HttpBinHeadersModel) {
                req = request
                res = response
                data = value

                lock.countDown()
            }

            override fun failure(request: Request, response: Response, error: FuelError) {
            }

        })

        await()

        assertNotNull(req, "request should not be null")
        assertNotNull(res, "response should not be null")
        assertNull(err, "error should be null")
        assertNotNull(data, "data should not be null")
        assertTrue(data is HttpBinHeadersModel, "data should be HttpBinHeadersModel type")
        assertFalse((data as HttpBinHeadersModel).headers.isEmpty(), "model must properly be serialized")
    }

}