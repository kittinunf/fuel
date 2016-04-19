package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.rx.rx_response
import com.github.kittinunf.fuel.rx.rx_responseObject
import com.github.kittinunf.fuel.rx.rx_responseString
import com.github.kittinunf.fuel.rx.rx_string
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Test
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RxFuelTest {

    init {
        FuelManager.instance.basePath = "https://httpbin.org"

        Fuel.testMode {
            timeout = 15000
        }
    }

    @Test
    fun rxTestResponse() {
        var called = false
        var response: Response? = null
        var data: ByteArray? = null
        Fuel.get("/get").rx_response().subscribe {
            val (r, d) = it
            response = r
            data = d
            called = true
        }

        assertThat(called, isEqualTo(true))
        assertThat(response, notNullValue())
        assertThat(data, notNullValue())
    }


    @Test
    fun rxTestResponseString() {
        var called = false
        var response: Response? = null
        var data: String? = null
        Fuel.get("/get").rx_responseString().subscribe {
            val (r, d) = it
            response = r
            data = d
            called = true
        }

        assertThat(called, isEqualTo(true))
        assertThat(response, notNullValue())
        assertThat(data, notNullValue())
    }

    @Test
    fun rxTestString() {
        var called = false
        var data: String? = null
        Fuel.get("/get").rx_string().subscribe {
            data = it
            called = true
        }

        assertThat(called, isEqualTo(true))
        assertThat(data, notNullValue())
    }

    @Test
    fun rxTestStringError() {
        var calledError = false
        var data: String? = null
        var error: Throwable? = null
        Fuel.get("/gt").rx_string().subscribe({
            data = it
        }, { ex ->
            error = ex
            calledError = true
        })

        assertThat(calledError, isEqualTo(true))
        assertThat(data, nullValue())
        assertThat(error, notNullValue())
    }

    //Model
    data class HttpBinUserAgentModel(var userAgent: String = "")

    //Deserializer
    class HttpBinUserAgentModelDeserializer : ResponseDeserializable<HttpBinUserAgentModel> {

        override fun deserialize(content: String): HttpBinUserAgentModel {
            return HttpBinUserAgentModel(content)
        }

    }

    @Test
    fun rxTestResponseObject() {
        var called = false
        var response: Response? = null
        var model: HttpBinUserAgentModel? = null
        Fuel.get("/user-agent").rx_responseObject(HttpBinUserAgentModelDeserializer()).subscribe {
            val (r, d) = it
            response = r
            model = d
            called = true
        }

        assertThat(called, isEqualTo(true))
        assertThat(response, notNullValue())
        assertThat(model, notNullValue())
    }

    @Test
    fun rxTestResponseObjectError() {
        var calledError = false
        var response: Response? = null
        var model: HttpBinUserAgentModel? = null
        var error: Throwable? = null
        Fuel.get("/useragent").rx_responseObject(HttpBinUserAgentModelDeserializer()).subscribe({
            val (r, d) = it
            response = r
            model = d
        }, { ex ->
            calledError = true
            error = ex
        })

        assertThat(calledError, isEqualTo(true))
        assertThat(response, nullValue())
        assertThat(model, nullValue())
        assertThat(error, notNullValue())
    }


}
