package com.github.kittinunf.fuel.forge

import com.github.kittinunf.forge.core.JSON
import com.github.kittinunf.forge.core.apply
import com.github.kittinunf.forge.core.at
import com.github.kittinunf.forge.core.map
import com.github.kittinunf.forge.core.maybeAt
import com.github.kittinunf.forge.util.create
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.ResponseHandler
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.test.MockHelper
import com.github.kittinunf.result.Result
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.isA
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.json.JSONException
import org.junit.After
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
        mock = MockHelper()
        mock.setup()
    }

    @After
    fun tearDown() {
        mock.tearDown()
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
                    assertThat(result.component1(), notNullValue())
                    assertThat(result.component2(), notNullValue())
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
                    assertThat(result.component1(), notNullValue())
                    assertThat(result.component2(), instanceOf(Result.Failure::class.java))
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
                    assertThat(result.component1(), notNullValue())
                    assertThat(result.component2(), notNullValue())
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
                    assertThat(result.component1(), notNullValue())
                    assertThat(result.component2(), instanceOf(Result.Failure::class.java))
                }
    }

    @Test
    fun forgeTestResponseHandlerObject() {
        mock.chain(
                request = mock.request().withPath("/user-agent"),
                response = mock.reflect()
        )

        Fuel.get(mock.path("user-agent"))
                .responseObject(httpBinUserDeserializer, object : ResponseHandler<HttpBinUserAgentModel> {
                    override fun success(request: Request, response: Response, value: HttpBinUserAgentModel) {
                        assertThat(value, notNullValue())
                    }

                    override fun failure(request: Request, response: Response, error: FuelError) {
                        assertThat(error, notNullValue())
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
                .responseObjects(issueInfoDeserializer, object : ResponseHandler<List<IssueInfo>> {
                    override fun success(request: Request, response: Response, value: List<IssueInfo>) {
                        assertThat(value, notNullValue())
                        assertThat(value.size, not(equalTo(0)))
                    }

                    override fun failure(request: Request, response: Response, error: FuelError) {
                        assertThat(error, notNullValue())
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
                .responseObject(httpBinUserDeserializer, object : ResponseHandler<HttpBinUserAgentModel> {
                    override fun success(request: Request, response: Response, value: HttpBinUserAgentModel) {
                        assertThat(value, notNullValue())
                    }

                    override fun failure(request: Request, response: Response, error: FuelError) {
                        assertThat(error, instanceOf(Result.Failure::class.java))
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
        assertThat(result.component1(), notNullValue())
        assertThat(result.component2(), notNullValue())
        assertThat(result.component3(), notNullValue())
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
        assertThat(result.component1(), notNullValue())
        assertThat(result.component2(), notNullValue())
        assertThat(result.component3(), notNullValue())
        assertThat(result.component3().component1()?.size, not(equalTo(0)))
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
            assertThat(issues.size, not(equalTo(0)))
            assertThat(issues[0], isA(IssueInfo::class.java))
        }
    }

    @Test
    fun forgeTestDeserializeItem() {
        val content = """ { "id": 123, "title": "title1", "number": 1 } """

        val issue = forgeDeserializerOf(issueInfoDeserializer).deserialize(content)

        assertThat(issue, notNullValue())
        assertThat(issue?.id, equalTo(123))
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

        assertThat(issues, notNullValue())
        assertThat(issues?.size, equalTo(2))
        assertThat(issues?.first()?.id, equalTo(123))
    }

    @Test(expected = JSONException::class)
    fun forgeTestInvalidItemsDeserializer() {
        val content = """ { "id": 123, "title": "title1", "number": 1 } """

        forgesDeserializerOf(issueInfoDeserializer).deserialize(content)
    }
}
