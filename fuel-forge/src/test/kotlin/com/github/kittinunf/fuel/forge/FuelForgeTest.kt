package com.github.kittinunf.fuel.forge

import com.github.kittinunf.forge.core.JSON
import com.github.kittinunf.forge.core.apply
import com.github.kittinunf.forge.core.at
import com.github.kittinunf.forge.core.map
import com.github.kittinunf.forge.core.maybeAt
import com.github.kittinunf.forge.util.create
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Handler
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result
import org.hamcrest.CoreMatchers
import org.json.JSONException
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection

class FuelForgeTest {

    init {
        Fuel.testMode {
            timeout = 15000
        }
    }

    private lateinit var mock: MockHelper

    @Before
    fun setup() {
        this.mock = MockHelper()
        this.mock.setup()
    }

    @After
    fun tearDown() {
        this.mock.tearDown()
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
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        Fuel.get(mock.path("user-agent"))
                .responseObject(httpBinUserDeserializer) { _, _, result ->
                    Assert.assertThat(result.component1(), CoreMatchers.notNullValue())
                    Assert.assertThat(result.component2(), CoreMatchers.notNullValue())
                }
    }

    @Test
    fun forgeTestResponseObjectError() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_BAD_REQUEST)
        )

        Fuel.get(mock.path("user-agent"))
                .responseObject(httpBinUserDeserializer) { _, _, result ->
                    Assert.assertThat(result.component1(), CoreMatchers.notNullValue())
                    Assert.assertThat(result.component2(), CoreMatchers.instanceOf(Result.Failure::class.java))
                }
    }

    @Test
    fun forgeTestResponseDeserializerObject() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        Fuel.get(mock.path("user-agent"))
                .responseObject(httpBinUserDeserializer) { _, _, result ->
                    Assert.assertThat(result.component1(), CoreMatchers.notNullValue())
                    Assert.assertThat(result.component2(), CoreMatchers.notNullValue())
                }
    }

    @Test
    fun forgeTestResponseDeserializerObjectError() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        Fuel.get(mock.path("user-agent"))
                .responseObject(httpBinUserDeserializer) { _, _, result ->
                    Assert.assertThat(result.component1(), CoreMatchers.notNullValue())
                    Assert.assertThat(result.component2(), CoreMatchers.instanceOf(Result.Failure::class.java))
                }
    }

    @Test
    fun forgeTestResponseHandlerObject() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        Fuel.get(mock.path("user-agent"))
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
        mock.chain(
            request = mock.request().withPath("/issues"),
            response = mock.response().withBody("[ " +
                "{ \"id\": 1, \"title\": \"issue 1\", \"number\": null }, " +
                "{ \"id\": 2, \"title\": \"issue 2\", \"number\": 32 }, " +
            " ]").withStatusCode(HttpURLConnection.HTTP_OK)
        )

        Fuel.get(mock.path("issues"))
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
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_BAD_REQUEST)
        )

        Fuel.get(mock.path("user-agent"))
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
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_BAD_REQUEST)
        )

        val result = Fuel.get(mock.path("user-agent")).responseObject(httpBinUserDeserializer)
        Assert.assertThat(result.component1(), CoreMatchers.notNullValue())
        Assert.assertThat(result.component2(), CoreMatchers.notNullValue())
        Assert.assertThat(result.component3(), CoreMatchers.notNullValue())
    }

    @Test
    fun forgeTestResponseObjectsWithNoHandler() {
        mock.chain(
            request = mock.request().withPath("/issues"),
            response = mock.response().withBody("[ " +
                    "{ \"id\": 1, \"title\": \"issue 1\", \"number\": null }, " +
                    "{ \"id\": 2, \"title\": \"issue 2\", \"number\": 32 }, " +
                    " ]").withStatusCode(HttpURLConnection.HTTP_OK)
        )

        val result = Fuel.get(mock.path("issues")).responseObjects(issueInfoDeserializer)
        Assert.assertThat(result.component1(), CoreMatchers.notNullValue())
        Assert.assertThat(result.component2(), CoreMatchers.notNullValue())
        Assert.assertThat(result.component3(), CoreMatchers.notNullValue())
        Assert.assertNotEquals(result.component3().component1()?.size, 0)
    }

    @Test
    fun forgeTestProcessingGenericList() {
        mock.chain(
            request = mock.request().withPath("/issues"),
            response = mock.response().withBody("[ " +
                    "{ \"id\": 1, \"title\": \"issue 1\", \"number\": null }, " +
                    "{ \"id\": 2, \"title\": \"issue 2\", \"number\": 32 }, " +
                    " ]").withStatusCode(HttpURLConnection.HTTP_OK)
        )

        Fuel.get(mock.path("issues")).responseObjects(issueInfoDeserializer) { _, _, result ->
            val issues = result.get()
            Assert.assertNotEquals(issues.size, 0)
            Assert.assertThat(issues[0], CoreMatchers.isA(IssueInfo::class.java))
        }
    }

    @Test
    fun forgeTestDeserializeItem() {
        val content = """ { "id": 123, "title": "title1", "number": 1 } """

        val issue = forgeDeserializerOf(issueInfoDeserializer).deserialize(content)

        Assert.assertThat(issue, CoreMatchers.notNullValue())
        Assert.assertThat(issue?.id, CoreMatchers.equalTo(123))
    }

    @Test(expected = JSONException::class)
    fun forgeTestInvalidItemDeserializer() {
        val content = """ [
            { "id": 123, "title": "title1", "number": 1 },
            { "id": 456, "title": "title2" }
        ] """

        forgeDeserializerOf(issueInfoDeserializer).deserialize(content)
    }

    @Test
    fun forgeTestDeserializeItems() {
        val content = """ [
            { "id": 123, "title": "title1", "number": 1 },
            { "id": 456, "title": "title2" }
        ] """

        val issues = forgesDeserializerOf(issueInfoDeserializer).deserialize(content)

        Assert.assertThat(issues, CoreMatchers.notNullValue())
        Assert.assertThat(issues?.size, CoreMatchers.equalTo(2))
        Assert.assertThat(issues?.first()?.id, CoreMatchers.equalTo(123))
    }

    @Test(expected = JSONException::class)
    fun forgeTestInvalidItemsDeserializer() {
        val content = """ { "id": 123, "title": "title1", "number": 1 } """

        forgesDeserializerOf(issueInfoDeserializer).deserialize(content)
    }
}
