package com.github.babedev.fuel.forge

import com.github.kittinunf.forge.core.*
import com.github.kittinunf.forge.util.create
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.result.Result
import org.hamcrest.CoreMatchers
import org.json.JSONException
import org.junit.Assert
import org.junit.Test

class FuelForgeTest {

    init {
        FuelManager.instance.basePath = "https://httpbin.org"

        Fuel.testMode {
            timeout = 15000
        }
    }

    data class HttpBinUserAgentModel(var userAgent: String = "", var status: String = "")
    data class IssueInfo(val id: Int, val title: String, val number: Int?)

    private val httpBinUserDeserializer = { json: JSON ->
        ::HttpBinUserAgentModel.create
                .map(json at "userAgent")
                .apply(json at "status")
    }

    private val issueInfoDeserializer = { json: JSON ->
        ::IssueInfo.create
                .map(json at "id")
                .apply(json at "title")
                .apply(json maybeAt "number")
    }

    @Test
    fun forgeTestResponseObject() {
        Fuel.get("/user-agent")
                .responseObject(httpBinUserDeserializer) { _, _, result ->
                    Assert.assertThat(result.component1(), CoreMatchers.notNullValue())
                    Assert.assertThat(result.component2(), CoreMatchers.notNullValue())
                }
    }

    @Test
    fun forgeTestResponseObjectError() {
        Fuel.get("/useragent")
                .responseObject(httpBinUserDeserializer) { _, _, result ->
                    Assert.assertThat(result.component1(), CoreMatchers.notNullValue())
                    Assert.assertThat(result.component2(), CoreMatchers.instanceOf(Result.Failure::class.java))
                }
    }

    @Test
    fun forgeTestResponseDeserializerObject() {
        Fuel.get("/user-agent")
                .responseObject(httpBinUserDeserializer) { _, _, result ->
                    Assert.assertThat(result.component1(), CoreMatchers.notNullValue())
                    Assert.assertThat(result.component2(), CoreMatchers.notNullValue())
                }
    }

    @Test
    fun forgeTestResponseDeserializerObjectError() {
        Fuel.get("/useragent")
                .responseObject(httpBinUserDeserializer) { _, _, result ->
                    Assert.assertThat(result.component1(), CoreMatchers.notNullValue())
                    Assert.assertThat(result.component2(), CoreMatchers.instanceOf(Result.Failure::class.java))
                }
    }

    @Test
    fun forgeTestResponseHandlerObject() {
        Fuel.get("/user-agent")
                .responseObject(httpBinUserDeserializer, object : Handler<HttpBinUserAgentModel> {
                    override fun success(request: Request, response: Response, value: HttpBinUserAgentModel) {
                        Assert.assertThat(value, CoreMatchers.notNullValue())
                    }

                    override fun failure(request: Request, response: Response, error: FuelError) {
                        Assert.assertThat(error, CoreMatchers.notNullValue())
                    }

                })
    }

    @Test
    fun forgeTestResponseHandlerObjects() {
        Fuel.get("https://api.github.com/repos/kittinunf/Fuel/issues")
                .responseObjects(issueInfoDeserializer, object : Handler<List<IssueInfo>> {
                    override fun success(request: Request, response: Response, value: List<IssueInfo>) {
                        Assert.assertThat(value, CoreMatchers.notNullValue())
                        Assert.assertNotEquals(value.size, 0)
                    }

                    override fun failure(request: Request, response: Response, error: FuelError) {
                        Assert.assertThat(error, CoreMatchers.notNullValue())
                    }

                })
    }

    @Test
    fun forgeTestResponseHandlerObjectError() {
        Fuel.get("/useragent")
                .responseObject(httpBinUserDeserializer, object : Handler<HttpBinUserAgentModel> {
                    override fun success(request: Request, response: Response, value: HttpBinUserAgentModel) {
                        Assert.assertThat(value, CoreMatchers.notNullValue())
                    }

                    override fun failure(request: Request, response: Response, error: FuelError) {
                        Assert.assertThat(error, CoreMatchers.instanceOf(Result.Failure::class.java))
                    }

                })
    }

    @Test
    fun forgeTestResponseObjectWithNoHandler() {
        val result = Fuel.get("/useragent").responseObject(httpBinUserDeserializer)
        Assert.assertThat(result.component1(), CoreMatchers.notNullValue())
        Assert.assertThat(result.component2(), CoreMatchers.notNullValue())
        Assert.assertThat(result.component3(), CoreMatchers.notNullValue())
    }

    @Test
    fun forgeTestResponseObjectsWithNoHandler() {
        val result = Fuel.get("https://api.github.com/repos/kittinunf/Fuel/issues").responseObjects(issueInfoDeserializer)
        Assert.assertThat(result.component1(), CoreMatchers.notNullValue())
        Assert.assertThat(result.component2(), CoreMatchers.notNullValue())
        Assert.assertThat(result.component3(), CoreMatchers.notNullValue())
        Assert.assertNotEquals(result.component3().component1()?.size, 0)
    }

    @Test
    fun forgeTestProcessingGenericList() {
        Fuel.get("https://api.github.com/repos/kittinunf/Fuel/issues").responseObjects(issueInfoDeserializer) { _, _, result ->
                    val issues = result.get()
                    Assert.assertNotEquals(issues.size, 0)
                    Assert.assertThat(issues[0], CoreMatchers.isA(IssueInfo::class.java))
                }
    }

    @Test
    fun forgeTestDeserializeSingleItem() {
        val content = """ { "id": 123, "title": "title1", "number": 1 } """

        val issue = forgeDeserializerOf(issueInfoDeserializer).deserialize(content)

        Assert.assertThat(issue, CoreMatchers.notNullValue())
        Assert.assertThat(issue?.id, CoreMatchers.equalTo(123))
    }

    @Test(expected = JSONException::class)
    fun forgeTestInvalidDeserializer() {
        val content = """ [
            { "id": 123, "title": "title1", "number": 1 },
            { "id": 456, "title": "title2" }
        ] """

        forgeDeserializerOf(issueInfoDeserializer).deserialize(content)
    }
}