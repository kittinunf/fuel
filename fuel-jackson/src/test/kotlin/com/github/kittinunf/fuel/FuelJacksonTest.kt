package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Handler
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.jackson.jacksonDeserializerOf
import com.github.kittinunf.fuel.jackson.responseObject
import com.github.kittinunf.result.Result
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.isA
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThat
import org.junit.Test
import java.net.HttpURLConnection

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
                    assertThat(result.component1()?.userAgent, not(""))
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
        val (_, res, result) = Fuel.get("https://api.github.com/repos/kittinunf/Fuel/issues/1").responseObject<IssueInfo>()
        assertThat(res, notNullValue())
        assertThat(result.get(), notNullValue())
        assertThat(result.get(), isA(IssueInfo::class.java))
        assertThat(result, notNullValue())
    }

    @Test
    fun jacksonTestResponseSyncObjectError() {
        val (_, res, result) = Fuel.get("https://api.github.com/repos/kittinunf/Fuel/issue/1").responseObject<IssueInfo>()
        assertThat(res, notNullValue())
        assertThat(result, notNullValue())
        val (value, error) = result
        assertThat(value, nullValue())
        assertThat(error, notNullValue())
        assertThat((error as FuelError).response.statusCode, equalTo(HttpURLConnection.HTTP_NOT_FOUND))
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
            issueList = jacksonDeserializerOf<List<IssueInfo>>().deserialize(response.dataStream)!!
            assertThat(issueList[0], isA(IssueInfo::class.java))
            issueList = jacksonDeserializerOf<List<IssueInfo>>().deserialize(response.dataStream.reader())!!
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
