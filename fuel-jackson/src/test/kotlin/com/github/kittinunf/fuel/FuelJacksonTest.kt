package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.result.Result
import com.github.kittiunf.fuel.jackson.jacksonDeserializerOf
import com.github.kittiunf.fuel.jackson.responseObject
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Test

class FuelJacksonTest {

    init {
        FuelManager.instance.basePath = "https://httpbin.org"

        Fuel.testMode {
            timeout = 15000
        }
    }

    //Model
    data class HttpBinUserAgentModel(var userAgent: String = "")

    @Test
    fun jacksonTestResponseObject() {
        Fuel.get("/user-agent")
                .responseObject(jacksonDeserializerOf<HttpBinUserAgentModel>()) { _, _, result ->
                    assertThat(result.component1(), notNullValue())
                    assertThat(result.component2(), notNullValue())
                }
    }

    @Test
    fun jacksonTestResponseObjectError() {
        Fuel.get("/useragent")
                .responseObject(jacksonDeserializerOf<HttpBinUserAgentModel>()) { _, _, result ->
                    assertThat(result.component1(), notNullValue())
                    assertThat(result.component2(), instanceOf(Result.Failure::class.java))
                }
    }

    @Test
    fun jacksonTestResponseDeserializerObject() {
        Fuel.get("/user-agent")
                .responseObject<HttpBinUserAgentModel> { _, _, result ->
                    assertThat(result.component1(), notNullValue())
                    assertThat(result.component2(), notNullValue())
                }
    }

    @Test
    fun jacksonTestResponseDeserializerObjectError() {
        Fuel.get("/useragent")
                .responseObject<HttpBinUserAgentModel> { _, _, result ->
                    assertThat(result.component1(), notNullValue())
                    assertThat(result.component2(), instanceOf(Result.Failure::class.java))
                }
    }

    @Test
    fun jacksonTestResponseHandlerObject() {
        Fuel.get("/user-agent")
                .responseObject(object : Handler<HttpBinUserAgentModel> {
                    override fun success(request: Request, response: Response, value: HttpBinUserAgentModel) {
                        assertThat(value, notNullValue())
                    }

                    override fun failure(request: Request, response: Response, error: FuelError) {
                        assertThat(error, notNullValue())
                    }

                })
    }

    @Test
    fun jacksonTestResponseHandlerObjectError() {
        Fuel.get("/useragent")
                .responseObject(object : Handler<HttpBinUserAgentModel> {
                    override fun success(request: Request, response: Response, value: HttpBinUserAgentModel) {
                        assertThat(value, notNullValue())
                    }

                    override fun failure(request: Request, response: Response, error: FuelError) {
                        assertThat(error, instanceOf(Result.Failure::class.java))
                    }

                })
    }

    @Test
    fun jacksonTestResponseSyncObject() {
        val triple = Fuel.get("/user-agent").responseObject<HttpBinUserAgentModel>()
        assertThat(triple.third.component1(), notNullValue())
    }

    @Test
    fun jacksonTestResponseSyncObjectError() {
        val triple = Fuel.get("/useragent").responseObject<HttpBinUserAgentModel>()
        assertThat(triple.third.component2(), instanceOf(FuelError::class.java))
    }
}
