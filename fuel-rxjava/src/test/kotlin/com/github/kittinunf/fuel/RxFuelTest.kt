package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.rx.rx_response
import com.github.kittinunf.fuel.rx.rx_responseObject
import com.github.kittinunf.fuel.rx.rx_responseString
import com.github.kittinunf.fuel.rx.rx_string
import org.hamcrest.CoreMatchers.notNullValue
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
        val (response, data) = Fuel.get("/get").rx_response()
                .test()
                .awaitTerminalEvent()
                .assertNoErrors()
                .assertValueCount(1)
                .assertCompleted()
                .onNextEvents[0]

        assertThat(response, notNullValue())
        assertThat(data, notNullValue())
    }


    @Test
    fun rxTestResponseString() {
        val (response, data) = Fuel.get("/get").rx_responseString()
                .test()
                .awaitTerminalEvent()
                .assertNoErrors()
                .assertValueCount(1)
                .assertCompleted()
                .onNextEvents[0]

        assertThat(response, notNullValue())
        assertThat(data, notNullValue())
    }

    @Test
    fun rxTestString() {
        val data = Fuel.get("/get").rx_string()
                .test()
                .awaitTerminalEvent()
                .assertNoErrors()
                .assertValueCount(1)
                .assertCompleted()
                .onNextEvents[0]

        assertThat(data, notNullValue())
    }

    @Test
    fun rxTestStringError() {
        Fuel.get("/gt").rx_string()
                .test()
                .awaitTerminalEvent()
                .assertNoValues()
                .assertError(Throwable::class.java)
                .assertNotCompleted()
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
        val (response, model) = Fuel.get("/user-agent")
                .rx_responseObject(HttpBinUserAgentModelDeserializer())
                .test()
                .awaitTerminalEvent()
                .assertNoErrors()
                .assertValueCount(1)
                .assertCompleted()
                .onNextEvents[0]

        assertThat(response, notNullValue())
        assertThat(model, notNullValue())
    }

    @Test
    fun rxTestResponseObjectError() {
        Fuel.get("/useragent").rx_responseObject(HttpBinUserAgentModelDeserializer())
                .test()
                .awaitTerminalEvent()
                .assertNoValues()
                .assertError(Throwable::class.java)
                .assertNotCompleted()
    }


}
