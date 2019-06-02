package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.ResponseHandler
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.gson.gsonDeserializerOf
import com.github.kittinunf.fuel.gson.jsonBody
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.test.MockHttpTestCase
import com.github.kittinunf.fuel.test.MockReflected
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Assert.fail
import org.junit.Test
import java.lang.reflect.Type
import java.net.HttpURLConnection

private typealias IssuesList = List<IssueInfo>

private data class IssueInfo(
    val id: Int,
    val title: String,
    val number: Int?
) {
    fun specialMethod() = "$id: $title"
}

private sealed class IssueType {
    object Bug : IssueType()
    object Feature : IssueType()

    companion object : JsonSerializer<IssueType>, JsonDeserializer<IssueType> {
        override fun serialize(src: IssueType, typeOfSrc: Type, context: JsonSerializationContext): JsonElement =
            when (src) {
                is Bug -> JsonPrimitive("bug")
                is Feature -> JsonPrimitive("feature")
            }

        override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): IssueType =
            if (json.isJsonPrimitive() && (json as JsonPrimitive).isString) {
                when (json.asString) {
                    "bug" -> Bug
                    "feature" -> Feature
                    else -> throw Error("Not a bug or a feature, what is it?")
                }
            } else {
                throw Error("String expected")
            }
    }
}

private typealias IssueTypeList = List<IssueType>

class FuelGsonTest : MockHttpTestCase() {

    data class HttpBinUserAgentModel(var userAgent: String = "")

    @Test
    fun gsonTestResponseObjectSync() {
        val (_, _, result) = reflectedRequest(Method.GET, "user-agent")
            .responseObject(gsonDeserializerOf(HttpBinUserAgentModel::class.java))

        val (data, error) = result
        assertThat("Expected data, actual error $error", data, notNullValue())
        assertThat("Expected data to have a user agent", data!!.userAgent, notNullValue())
    }

    @Test
    fun gsonTestResponseObjectAsync() {
        var isAsync = false
        val running = reflectedRequest(Method.GET, "user-agent")
            .responseObject(gsonDeserializerOf(HttpBinUserAgentModel::class.java)) { _, _, result ->
                val (data, error) = result
                assertThat("Expected data, actual error $error", data, notNullValue())
                assertThat("Expected data to have a user agent", data!!.userAgent, notNullValue())
                assertThat("Expected isAsync to be true, actual false", isAsync, equalTo(true))
            }

        isAsync = true
        running.join()

        assertThat(running.isDone, equalTo(true))
        assertThat(running.isCancelled, equalTo(false))
    }

    @Test
    fun gsonTestResponseObjectErrorSync() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        val (_, _, result) = Fuel.get(mock.path("user-agent"))
            .responseObject(gsonDeserializerOf(FuelGsonTest.HttpBinUserAgentModel::class.java))

        val (data, error) = result
        assertThat("Expected error, actual data $data", error, notNullValue())
    }

    @Test
    fun gsonTestResponseObjectErrorAsync() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        var isAsync = false
        val running = Fuel.get(mock.path("user-agent"))
            .responseObject(gsonDeserializerOf(HttpBinUserAgentModel::class.java)) { _, _, result ->
                val (data, error) = result
                assertThat("Expected error, actual data $data", error, notNullValue())
                assertThat("Expected isAsync to be true, actual false", isAsync, equalTo(true))
            }

        isAsync = true
        running.join()

        assertThat(running.isDone, equalTo(true))
        assertThat(running.isCancelled, equalTo(false))
    }

    @Test
    fun gsonTestResponseDeserializerObjectSync() {
        val (_, _, result) = reflectedRequest(Method.GET, "user-agent")
            .responseObject<FuelGsonTest.HttpBinUserAgentModel>()

        val (data, error) = result
        assertThat("Expected data, actual error $error", data, notNullValue())
        assertThat("Expected data to have a user agent", data!!.userAgent, notNullValue())
    }

    @Test
    fun gsonTestResponseDeserializerObjectAsync() {
        var isAsync = false
        val running = reflectedRequest(Method.GET, "user-agent")
            .responseObject<FuelGsonTest.HttpBinUserAgentModel>() { _, _, result ->
                val (data, error) = result
                assertThat("Expected data, actual error $error", data, notNullValue())
                assertThat("Expected data to have a user agent", data!!.userAgent, notNullValue())
                assertThat("Expected isAsync to be true, actual false", isAsync, equalTo(true))
            }

        isAsync = true
        running.join()

        assertThat(running.isDone, equalTo(true))
        assertThat(running.isCancelled, equalTo(false))
    }

    @Test
    fun gsonTestResponseDeserializerObjectErrorSync() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        val (_, _, result) = Fuel.get(mock.path("user-agent"))
            .responseObject<FuelGsonTest.HttpBinUserAgentModel>()

        val (data, error) = result
        assertThat("Expected error, actual data $data", error, notNullValue())
    }

    @Test
    fun gsonTestResponseDeserializerObjectErrorAsync() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        var isAsync = false
        val running = Fuel.get(mock.path("user-agent"))
            .responseObject<FuelGsonTest.HttpBinUserAgentModel>() { _, _, result ->
                val (data, error) = result
                assertThat("Expected error, actual data $data", error, notNullValue())
                assertThat("Expected isAsync to be true, actual false", isAsync, equalTo(true))
            }

        isAsync = true
        running.join()

        assertThat(running.isDone, equalTo(true))
        assertThat(running.isCancelled, equalTo(false))
    }

    @Test
    fun gsonTestResponseHandlerObjectAsync() {
        var isAsync = false
        val running = reflectedRequest(Method.GET, "user-agent")
            .responseObject(
                object : ResponseHandler<HttpBinUserAgentModel> {
                    override fun success(request: Request, response: Response, value: HttpBinUserAgentModel) {
                        assertThat(value, notNullValue())
                        assertThat("Expected value to have a user agent", value.userAgent, notNullValue())
                        assertThat("Expected isAsync to be true, actual false", isAsync, equalTo(true))
                    }

                    override fun failure(request: Request, response: Response, error: FuelError) {
                        fail("Expected request to succeed, actual $error")
                    }
                }
            )

        isAsync = true
        running.join()

        assertThat(running.isDone, equalTo(true))
        assertThat(running.isCancelled, equalTo(false))
    }

    @Test
    fun gsonTestResponseHandlerObjectErrorAsync() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        var isAsync = false
        val running = Fuel.get(mock.path("user-agent"))
            .responseObject(object : ResponseHandler<HttpBinUserAgentModel> {
                override fun success(request: Request, response: Response, value: HttpBinUserAgentModel) {
                    fail("Expected request to fail, actual $value")
                }

                override fun failure(request: Request, response: Response, error: FuelError) {
                    assertThat(error, notNullValue())
                    assertThat("Expected isAsync to be true, actual false", isAsync, equalTo(true))
                }
            })

        isAsync = true
        running.join()

        assertThat(running.isDone, equalTo(true))
        assertThat(running.isCancelled, equalTo(false))
    }

    /**
     * Test for https://github.com/kittinunf/Fuel/issues/233
     */
    @Test
    fun testProcessingGenericList() {
        mock.chain(
            request = mock.request().withPath("/issues"),
            response = mock.response().withBody("[ " +
                    "{ \"id\": 1, \"title\": \"issue 1\", \"number\": null }, " +
                    "{ \"id\": 2, \"title\": \"issue 2\", \"number\": 32 }, " +
                    " ]").withStatusCode(HttpURLConnection.HTTP_OK)
        )

        val (_, _, result) = Fuel.get(mock.path("issues")).responseObject<IssuesList>()

        val (issues, error) = result
        assertThat("Expected issues, actual error $error", issues, notNullValue())
        assertThat(issues!!.size, not(equalTo(0)))
        assertThat(issues.first().specialMethod(), equalTo("1: issue 1"))
    }

    @Test
    fun testSettingJsonBody() {
        val data = listOf(
            IssueInfo(id = 1, title = "issue 1", number = null),
            IssueInfo(id = 2, title = "issue 2", number = 32)
        )

        val (_, _, result) = reflectedRequest(Method.POST, "json-body")
            .jsonBody(data)
            .responseObject<MockReflected>()

        val (reflected, error) = result
        val issues: IssuesList = Gson().fromJson(reflected!!.body!!.string!!, object : TypeToken<IssuesList>() {}.type)
        assertThat("Expected issues, actual error $error", issues, notNullValue())
        assertThat(issues.size, equalTo(data.size))
        assertThat(issues.first().specialMethod(), equalTo("1: issue 1"))
    }

    @Test
    fun testCustomGsonInstance() {
        val gson = GsonBuilder()
            .registerTypeAdapter(IssueType::class.java, IssueType.Companion)
            .create()

        val data = listOf(
            IssueType.Bug,
            IssueType.Feature
        )

        val (_, _, result) = reflectedRequest(Method.POST, "json-body")
            .jsonBody(data, gson)
            .responseObject<MockReflected>()

        val (reflected, error) = result
        val body = reflected!!.body!!.string!!
        val types: IssueTypeList = gson.fromJson(body, object : TypeToken<IssueTypeList>() {}.type)
        assertThat("Expected types, actual error $error", types, notNullValue())
        assertThat(body, equalTo("[\"bug\",\"feature\"]"))
        assertThat(types.size, equalTo(data.size))
        assertThat(types.first(), equalTo<IssueType>(IssueType.Bug))
    }
}
