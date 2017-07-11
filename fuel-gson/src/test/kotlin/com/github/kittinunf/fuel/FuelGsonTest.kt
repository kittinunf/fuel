package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.fuel.gson.GsonDeserializer
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.result.Result
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Test

/**
 * Created by ihor_kucherenko on 7/4/17.
 * https://github.com/KucherenkoIhor
 */
class FuelGsonTest {

    init {
        FuelManager.instance.basePath = "https://httpbin.org"

        Fuel.testMode {
            timeout = 15000
        }
    }

    //Model
    data class HttpBinUserAgentModel(var userAgent: String = "")

    @Test
    fun gsonTestResponseObject() {
        Fuel.get("/user-agent")
                .responseObject(GsonDeserializer<HttpBinUserAgentModel>()) { _, _, result ->
                    MatcherAssert.assertThat(result.component1(), CoreMatchers.notNullValue())
                    MatcherAssert.assertThat(result.component2(), CoreMatchers.notNullValue())
                }
    }

    @Test
    fun gsonTestResponseObjectError() {
        Fuel.get("/useragent")
                .responseObject(GsonDeserializer<HttpBinUserAgentModel>()) { _, _, result ->
                    MatcherAssert.assertThat(result.component1(), CoreMatchers.notNullValue())
                    MatcherAssert.assertThat(result.component2(), CoreMatchers.instanceOf(Result.Failure::class.java))
                }
    }

    @Test
    fun gsonTestResponseDeserializerObject() {
        Fuel.get("/user-agent")
                .responseObject { _: Request, _: Response, result: Result<HttpBinUserAgentModel, FuelError> ->
                    MatcherAssert.assertThat(result.component1(), CoreMatchers.notNullValue())
                    MatcherAssert.assertThat(result.component2(), CoreMatchers.notNullValue())
                }
    }

    @Test
    fun gsonTestResponseDeserializerObjectError() {
        Fuel.get("/useragent")
                .responseObject { _: Request, _: Response, result: Result<HttpBinUserAgentModel, FuelError> ->
                    MatcherAssert.assertThat(result.component1(), CoreMatchers.notNullValue())
                    MatcherAssert.assertThat(result.component2(), CoreMatchers.instanceOf(Result.Failure::class.java))
                }
    }

    @Test
    fun gsonTestResponseHandlerObject() {
        Fuel.get("/user-agent")
                .responseObject(object : Handler<HttpBinUserAgentModel> {
                    override fun success(request: Request, response: Response, value: HttpBinUserAgentModel) {
                        MatcherAssert.assertThat(value, CoreMatchers.notNullValue())
                    }

                    override fun failure(request: Request, response: Response, error: FuelError) {
                        MatcherAssert.assertThat(error, CoreMatchers.notNullValue())
                    }

                })
    }

    @Test
    fun gsonTestResponseHandlerObjectError() {
        Fuel.get("/useragent")
                .responseObject(object : Handler<HttpBinUserAgentModel> {
                    override fun success(request: Request, response: Response, value: HttpBinUserAgentModel) {
                        MatcherAssert.assertThat(value, CoreMatchers.notNullValue())
                    }

                    override fun failure(request: Request, response: Response, error: FuelError) {
                        MatcherAssert.assertThat(error, CoreMatchers.instanceOf(Result.Failure::class.java))
                    }

                })
    }

    @Test
    fun gsonTestResponseSyncObject() {
        val triple = Fuel.get("/user-agent").responseObject<HttpBinUserAgentModel>()
        MatcherAssert.assertThat(triple.third.component1(), CoreMatchers.notNullValue())
    }

    @Test
    fun gsonTestResponseSyncObjectError() {
        val triple = Fuel.get("/useragent").responseObject<HttpBinUserAgentModel>()
        MatcherAssert.assertThat(triple.third.component2(), CoreMatchers.instanceOf(FuelError::class.java))
    }
}