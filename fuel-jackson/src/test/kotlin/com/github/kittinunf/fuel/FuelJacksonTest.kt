package com.github.kittinunf.fuel

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.HttpException
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.ResponseHandler
import com.github.kittinunf.fuel.jackson.jacksonDeserializerOf
import com.github.kittinunf.fuel.jackson.responseObject
import com.github.kittinunf.fuel.test.MockHttpTestCase
import com.github.kittinunf.result.Result
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.isA
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThat
import org.junit.Assert.fail
import org.junit.Test
import org.mockserver.matchers.Times
import java.net.HttpURLConnection

class FuelJacksonTest : MockHttpTestCase() {

    // Model
    data class HttpBinUserAgentModel(var userAgent: String = "")

    @Test
    fun jacksonTestResponseObject() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        Fuel.get(mock.path("user-agent"))
            .responseObject(jacksonDeserializerOf<HttpBinUserAgentModel>()) { _, _, result ->
                assertThat(result, instanceOf(Result.Success::class.java))
                with(result as Result.Success) {
                    assertThat(value, instanceOf(HttpBinUserAgentModel::class.java))
                    assertThat(value.userAgent, not(""))
                }
            }
            .get()
    }

    @Test
    fun jacksonTestResponseObjectWithCustomMapper() {
        mock.chain(
                request = mock.request().withPath("/user-agent"),
                response = mock.response()
                    .withBody("""{ "user_agent": "test" }""")
                    .withStatusCode(HttpURLConnection.HTTP_OK)
        )

        Fuel.get(mock.path("user-agent"))
            .responseObject(jacksonDeserializerOf<HttpBinUserAgentModel>(createCustomMapper())) { _, _, result ->
                assertThat(result, instanceOf(Result.Success::class.java))
                with(result as Result.Success) {
                    assertThat(value, instanceOf(HttpBinUserAgentModel::class.java))
                    assertThat(value.userAgent, not(""))
                }
            }
            .get()
    }

    @Test
    fun jacksonTestResponseObjectError() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        Fuel.get(mock.path("user-agent"))
            .responseObject(jacksonDeserializerOf<HttpBinUserAgentModel>()) { _, _, result ->
                assertThat(result, instanceOf(Result.Failure::class.java))
                with(result as Result.Failure) {
                    assertThat(error, notNullValue())
                }
            }
            .get()
    }

    @Test
    fun jacksonTestResponseObjectErrorWithCustomMapper() {
        mock.chain(
                request = mock.request().withPath("/user-agent"),
                response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        Fuel.get(mock.path("user-agent"))
            .responseObject(jacksonDeserializerOf<HttpBinUserAgentModel>(createCustomMapper())) { _, _, result ->
                assertThat(result, instanceOf(Result.Failure::class.java))
                with(result as Result.Failure) {
                    assertThat(error, notNullValue())
                }
            }
            .get()
    }

    @Test
    fun jacksonTestResponseDeserializerObject() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        Fuel.get(mock.path("user-agent"))
            .responseObject<HttpBinUserAgentModel> { _, _, result ->
                assertThat(result, instanceOf(Result.Success::class.java))
                with(result as Result.Success) {
                    assertThat(value, instanceOf(HttpBinUserAgentModel::class.java))
                    assertThat(value.userAgent, not(""))
                }
            }
            .get()
    }

    @Test
    fun jacksonTestResponseDeserializerObjectWithCustomMapper() {
        mock.chain(
                request = mock.request().withPath("/user-agent"),
                response = mock.response()
                    .withBody("""{ "user_agent": "test" }""")
                    .withStatusCode(HttpURLConnection.HTTP_OK)
        )

        Fuel.get(mock.path("user-agent"))
            .responseObject<HttpBinUserAgentModel>(createCustomMapper()) { _, _, result ->
                assertThat(result, instanceOf(Result.Success::class.java))
                with(result as Result.Success) {
                    assertThat(value, instanceOf(HttpBinUserAgentModel::class.java))
                    assertThat(value.userAgent, not(""))
                }
            }
            .get()
    }

    @Test
    fun jacksonTestResponseDeserializerObjectError() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        Fuel.get(mock.path("user-agent"))
            .responseObject<HttpBinUserAgentModel> { _, _, result ->
                assertThat(result, instanceOf(Result.Failure::class.java))
                with(result as Result.Failure) {
                    assertThat(error, notNullValue())
                }
            }
            .get()
    }

    @Test
    fun jacksonTestResponseHandlerObject() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        Fuel.get(mock.path("user-agent"))
            .responseObject(object : ResponseHandler<HttpBinUserAgentModel> {
                override fun success(request: Request, response: Response, value: HttpBinUserAgentModel) {
                    assertThat(value, notNullValue())
                }

                override fun failure(request: Request, response: Response, error: FuelError) {
                    fail("Request shouldn't have failed")
                }
            })
            .get()
    }

    @Test
    fun jacksonTestResponseHandlerObjectWithCustomMapper() {
        mock.chain(
                request = mock.request().withPath("/user-agent"),
                response = mock.reflect()
        )

        Fuel.get(mock.path("user-agent"))
            .responseObject(createCustomMapper(), object : ResponseHandler<HttpBinUserAgentModel> {
                override fun success(request: Request, response: Response, value: HttpBinUserAgentModel) {
                    assertThat(value, notNullValue())
                }

                override fun failure(request: Request, response: Response, error: FuelError) {
                    fail("Request shouldn't have failed")
                }
            })
            .get()
    }

    @Test
    fun jacksonTestResponseHandlerObjectError() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        Fuel.get(mock.path("user-agent"))
            .responseObject(createCustomMapper(), object : ResponseHandler<HttpBinUserAgentModel> {
                override fun success(request: Request, response: Response, value: HttpBinUserAgentModel) {
                    fail("Request should have failed")
                }

                override fun failure(request: Request, response: Response, error: FuelError) {
                    assertThat(error.exception, instanceOf(HttpException::class.java))
                }
            })
            .get()
    }

    @Test
    fun jacksonTestResponseSyncObject() {
        mock.chain(
            request = mock.request().withPath("/issues/1"),
            response = mock.response().withBody(
                    "{ \"id\": 1, \"title\": \"issue 1\", \"number\": null }"
            ).withStatusCode(HttpURLConnection.HTTP_OK)
        )

        val (_, res, result) = Fuel.get(mock.path("issues/1")).responseObject<IssueInfo>()
        assertThat(res, notNullValue())
        assertThat(result.get(), notNullValue())
        assertThat(result.get(), isA(IssueInfo::class.java))
        assertThat(result, notNullValue())
    }

    @Test
    fun jacksonTestResponseSyncObjectWithCustomMapper() {
        mock.chain(
                request = mock.request().withPath("/issues/1"),
                response = mock.response().withBody(
                        "{ \"id\": 1, \"title\": \"issue 1\", \"number\": null, \"snake_property\": 10 }"
                ).withStatusCode(HttpURLConnection.HTTP_OK)
        )

        val (_, res, result) = Fuel.get(mock.path("issues/1")).responseObject<IssueInfo>(createCustomMapper())
        assertThat(res, notNullValue())
        assertThat(result.get(), notNullValue())
        assertThat(result.get(), isA(IssueInfo::class.java))
        assertThat(result.get().snakeProperty, equalTo(10))
        assertThat(result, notNullValue())
    }

    @Test
    fun jacksonTestResponseSyncObjectError() {
        mock.chain(
            request = mock.request().withPath("/issues/1"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )
        val (_, res, result) = Fuel.get(mock.path("issues/1")).responseObject<IssueInfo>()
        assertThat(res, notNullValue())
        assertThat(result, notNullValue())
        val (value, error) = result
        assertThat(value, nullValue())
        assertThat(error, notNullValue())
        assertThat((error as FuelError).response.statusCode, equalTo(HttpURLConnection.HTTP_NOT_FOUND))
    }

    data class IssueInfo(val id: Int, val title: String, val number: Int, val snakeProperty: Int)

    @Test
    fun testProcessingGenericList() {
        mock.chain(
            request = mock.request().withPath("/issues"),
            response = mock.response()
                .withBody(
                    """
                      [
                        {
                          "id": 1,
                          "title": "issue 1",
                          "number": null
                        },
                        {
                          "id": 2,
                          "title": "issue 2",
                          "number": 32
                        }
                      ]
                    """
                )
                .withStatusCode(HttpURLConnection.HTTP_OK)
        )

        Fuel.get(mock.path("issues"))
            .responseObject<List<IssueInfo>> { _, _, result ->
                val issues = result.get()
                assertNotEquals(issues.size, 0)
                assertThat(issues[0], isA(IssueInfo::class.java))
            }
            .get()
    }

    @Test
    fun manualDeserializationShouldWork() {
        mock.chain(
            request = mock.request().withPath("/issues"),
            response = mock.response()
                .withBody(
                    """
                      [
                        {
                          "id": 1,
                          "title": "issue 1",
                          "number": null
                        },
                        {
                          "id": 2,
                          "title": "issue 2",
                          "number": 32
                        }
                      ]
                    """
                )
                .withStatusCode(HttpURLConnection.HTTP_OK),
            times = Times.exactly(5)
        )

        Fuel.get(mock.path("issues"))
            .response { _: Request, response: Response, _ ->
                val issueList = jacksonDeserializerOf<List<IssueInfo>>().deserialize(response)
                assertThat(issueList[0], isA(IssueInfo::class.java))
            }
            .get()

        Fuel.get(mock.path("issues"))
            .response { _: Request, response: Response, _ ->
                val issueList = jacksonDeserializerOf<List<IssueInfo>>().deserialize(response.body().toStream())!!
                assertThat(issueList[0], isA(IssueInfo::class.java))
            }
            .get()

        Fuel.get(mock.path("issues"))
            .response { _: Request, response: Response, _ ->
                val issueList = jacksonDeserializerOf<List<IssueInfo>>().deserialize(response.body().toStream().reader())!!
                assertThat(issueList[0], isA(IssueInfo::class.java))
            }
            .get()

        Fuel.get(mock.path("issues"))
            .response { _: Request, _, result: Result<ByteArray, FuelError> ->
                val issueList = jacksonDeserializerOf<List<IssueInfo>>().deserialize(result.get())!!
                assertThat(issueList[0], isA(IssueInfo::class.java))
            }
            .get()

        Fuel.get(mock.path("issues"))
            .responseString { _: Request, _: Response, result: Result<String, FuelError> ->
                val issueList = jacksonDeserializerOf<List<IssueInfo>>().deserialize(result.get())!!
                assertThat(issueList[0], isA(IssueInfo::class.java))
            }
            .get()
    }

    private fun createCustomMapper(): ObjectMapper {
        val mapper = ObjectMapper().registerKotlinModule()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        mapper.propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE

        return mapper
    }
}
