package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.*
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.Reader
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RequestObjectTest : BaseTestCase() {

    init {
        FuelManager.instance.basePath = "http://httpbin.org"
    }

    //Model
    data class HttpBinUserAgentModel(var userAgent: String = "")

    //Deserializer
    class HttpBinUserAgentModelDeserializer : ResponseDeserializable<HttpBinUserAgentModel> {

        override fun deserialize(content: String): HttpBinUserAgentModel {
            return HttpBinUserAgentModel(content)
        }

    }

    class HttpBinMalformedDeserializer : ResponseDeserializable<HttpBinUserAgentModel> {

        override fun deserialize(reader: Reader): HttpBinUserAgentModel {
            throw IllegalStateException("Malformed data")
        }

    }

    @Test
    fun httpRequestObjectUserAgentValidTest() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        Fuel.get("user-agent").responseObject(HttpBinUserAgentModelDeserializer()) { req, res, result ->
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

        assertThat(data as HttpBinUserAgentModel, isA(HttpBinUserAgentModel::class.java))
        assertThat((data as HttpBinUserAgentModel).userAgent, isEqualTo(not("")))
    }

    @Test
    fun httpRequestObjectUserAgentInvalidTest() {
        var request: Request? = null
        var response: Response? = null
        var data: Any? = null
        var error: FuelError? = null

        Fuel.get("user-agent").responseObject(HttpBinMalformedDeserializer()) { req, res, result ->
            request = req
            response = res

            val (d, err) = result
            data = d
            error = err
        }

        assertThat(request, notNullValue())
        assertThat(response, notNullValue())
        assertThat(error, notNullValue())
        assertThat(data, nullValue())

        assertThat(error?.exception as IllegalStateException, isA(IllegalStateException::class.java))
    }

}