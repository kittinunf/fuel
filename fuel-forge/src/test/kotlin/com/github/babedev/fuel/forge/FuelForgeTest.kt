package com.github.babedev.fuel.forge

import com.github.kittinunf.forge.core.JSON
import com.github.kittinunf.forge.core.apply
import com.github.kittinunf.forge.core.at
import com.github.kittinunf.forge.core.map
import com.github.kittinunf.forge.util.create
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.result.Result
import org.hamcrest.CoreMatchers
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

    private val httpBinUserDeserializer = { json: JSON ->
        ::HttpBinUserAgentModel.create.
                map(json at "userAgent").
                apply(json at "status")
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

    data class IssueInfo(val id: Int, val title: String, val number: Int)

    private val issueInfoDeserializer = { json: JSON ->
        ::IssueInfo.create.
                map(json at "id").
                apply(json at "title").
                apply(json at "number")
    }

    @Test
    fun testProcessingGenericList() {
        Fuel.get("https://api.github.com/repos/kittinunf/Fuel/issues").responseObjects(issueInfoDeserializer) { _, _, result ->
            val issues = result.get()
            Assert.assertNotEquals(issues.size, 0)
            Assert.assertThat(issues[0], CoreMatchers.isA(IssueInfo::class.java))
        }
    }
}