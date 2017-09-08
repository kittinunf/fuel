package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.result.Result
import com.github.kittiunf.fuel.jackson.jacksonDeserializerOf
import com.github.kittiunf.fuel.jackson.responseObject
import org.hamcrest.Matchers.*
import org.junit.Assert.assertNotEquals
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
                    assertThat(result.component1(), instanceOf(HttpBinUserAgentModel::class.java))
                    assertThat(result.component1()?.userAgent, (not(isEmptyString())))
                    assertThat(result.component2(), instanceOf(FuelError::class.java))
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

    data class IssueInfo(val id: Int, val title: String, val number: Int)

    @Test
    fun testProcessingGenericList() {
        Fuel.get("https://api.github.com/repos/kittinunf/Fuel/issues").responseObject<List<IssueInfo>> { _, _, result ->
            val issues = result.get()
            assertNotEquals(issues.size, 0)
            assertThat(issues[0], isA(IssueInfo::class.java))
        }
    }

    @Test
    fun manualDeserializationShouldWork() {
        Fuel.get("https://api.github.com/repos/kittinunf/Fuel/issues").response { _: Request, response: Response, result: Result<ByteArray, FuelError> ->
            var issueList = jacksonDeserializerOf<List<IssueInfo>>().deserialize(response)
            assertThat(issueList[0], isA(IssueInfo::class.java))
            issueList = jacksonDeserializerOf<List<IssueInfo>>().deserialize(result.get())!!
            assertThat(issueList[0], isA(IssueInfo::class.java))
        }
        Fuel.get("https://api.github.com/repos/kittinunf/Fuel/issues").responseString { _: Request, _: Response, result: Result<String, FuelError> ->
            val issueList = jacksonDeserializerOf<List<IssueInfo>>().deserialize(result.get())!!
            assertThat(issueList[0], isA(IssueInfo::class.java))

        }

    }
}
