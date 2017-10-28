package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.github.kittinunf.fuel.moshi.responseObject
import com.github.kittinunf.result.Result
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

class FuelMoshiTest {

    init {
        FuelManager.instance.basePath = "https://httpbin.org"

        Fuel.testMode {
            timeout = 15000
        }
    }

    data class HttpBinUserAgentModel(var userAgent: String = "")

    @Test
    fun moshiTestResponseObject() {
        Fuel.get("/user-agent")
                .responseObject(moshiDeserializerOf<HttpBinUserAgentModel>()) { _, _, result ->
                    Assert.assertThat(result.component1(), CoreMatchers.notNullValue())
                    Assert.assertThat(result.component2(), CoreMatchers.notNullValue())
                }
    }

    @Test
    fun moshiTestResponseObjectError() {
        Fuel.get("/useragent")
                .responseObject(moshiDeserializerOf<HttpBinUserAgentModel>()) { _, _, result ->
                    Assert.assertThat(result.component1(), CoreMatchers.notNullValue())
                    Assert.assertThat(result.component2(), CoreMatchers.instanceOf(Result.Failure::class.java))
                }
    }

    @Test
    fun moshiTestResponseDeserializerObject() {
        Fuel.get("/user-agent")
                .responseObject<HttpBinUserAgentModel> { _, _, result ->
                    Assert.assertThat(result.component1(), CoreMatchers.notNullValue())
                    Assert.assertThat(result.component2(), CoreMatchers.notNullValue())
                }
    }

    @Test
    fun moshiTestResponseDeserializerObjectError() {
        Fuel.get("/useragent")
                .responseObject<HttpBinUserAgentModel> { _, _, result ->
                    Assert.assertThat(result.component1(), CoreMatchers.notNullValue())
                    Assert.assertThat(result.component2(), CoreMatchers.instanceOf(Result.Failure::class.java))
                }
    }

    @Test
    fun moshiTestResponseHandlerObject() {
        Fuel.get("/user-agent")
                .responseObject(object : Handler<HttpBinUserAgentModel> {
                    override fun success(request: Request, response: Response, value: HttpBinUserAgentModel) {
                        Assert.assertThat(value, CoreMatchers.notNullValue())
                    }

                    override fun failure(request: Request, response: Response, error: FuelError) {
                        Assert.assertThat(error, CoreMatchers.notNullValue())
                    }

                })
    }

    @Test
    fun moshiTestResponseHandlerObjectError() {
        Fuel.get("/useragent")
                .responseObject(object : Handler<HttpBinUserAgentModel> {
                    override fun success(request: Request, response: Response, value: HttpBinUserAgentModel) {
                        Assert.assertThat(value, CoreMatchers.notNullValue())
                    }

                    override fun failure(request: Request, response: Response, error: FuelError) {
                        Assert.assertThat(error, CoreMatchers.instanceOf(Result.Failure::class.java))
                    }

                })
    }

    @Test
    fun moshiTestResponseSyncObject() {
        val triple = Fuel.get("/user-agent").responseObject()
        Assert.assertThat(triple.third.component1(), CoreMatchers.notNullValue())
    }

    @Test
    fun moshiTestResponseSyncObjectError() {
        val triple = Fuel.get("/useragent").responseObject()
        Assert.assertThat(triple.third.component2(), CoreMatchers.instanceOf(FuelError::class.java))
    }

    data class IssueInfo(val id: Int, val title: String, val number: Int)

    /**
     * Test for https://github.com/kittinunf/Fuel/issues/233
     */
    @Test
    fun testProcessingGenericList() {
        Fuel.get("https://api.github.com/repos/kittinunf/Fuel/issues").responseObject<List<IssueInfo>> { _, _, result ->
            val issues = result.get()
            Assert.assertNotEquals(issues.size, 0)
            Assert.assertThat(issues[0], CoreMatchers.isA(IssueInfo::class.java))
        }
    }
}