package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Handler
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.github.kittinunf.fuel.moshi.responseObject
import com.github.kittinunf.result.Result
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThat
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
                    assertThat(result.component1(), notNullValue())
                    assertThat(result.component2(), notNullValue())
                }
    }

    @Test
    fun moshiTestResponseObjectError() {
        Fuel.get("/useragent")
                .responseObject(moshiDeserializerOf<HttpBinUserAgentModel>()) { _, _, result ->
                    assertThat(result.component1(), notNullValue())
                    assertThat(result.component2(), instanceOf(Result.Failure::class.java))
                }
    }

    @Test
    fun moshiTestResponseDeserializerObject() {
        Fuel.get("/user-agent")
                .responseObject<HttpBinUserAgentModel> { _, _, result ->
                    assertThat(result.component1(), notNullValue())
                    assertThat(result.component2(), notNullValue())
                }
    }

    @Test
    fun moshiTestResponseDeserializerObjectError() {
        Fuel.get("/useragent")
                .responseObject<HttpBinUserAgentModel> { _, _, result ->
                    assertThat(result.component1(), notNullValue())
                    assertThat(result.component2(), instanceOf(Result.Failure::class.java))
                }
    }

    @Test
    fun moshiTestResponseHandlerObject() {
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
    fun moshiTestResponseHandlerObjectError() {
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
    fun moshiTestResponseSyncObject() {
        val triple = Fuel.get("/user-agent").responseObject<HttpBinUserAgentModel>()
        assertThat(triple.third.component1(), notNullValue())
        assertThat(triple.third.component1(), instanceOf(HttpBinUserAgentModel::class.java))
    }

    @Test
    fun moshiTestResponseSyncObjectError() {
        val triple = Fuel.get("/useragent").responseObject<HttpBinUserAgentModel>()
        assertThat(triple.third.component2(), instanceOf(FuelError::class.java))
    }

    data class IssueInfo(val id: Int, val title: String, val number: Int)

    /**
     * Test for https://github.com/kittinunf/Fuel/issues/233
     */
    @Test
    fun testProcessingGenericList() {
        Fuel.get("https://api.github.com/repos/kittinunf/Fuel/issues").responseObject<List<IssueInfo>> { _, _, result ->
            val issues = result.get()
            assertNotEquals(issues.size, 0)
            assertThat(issues[0], instanceOf(IssueInfo::class.java))
        }
    }
}