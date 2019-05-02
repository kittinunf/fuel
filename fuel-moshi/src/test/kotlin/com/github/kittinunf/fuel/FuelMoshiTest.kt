package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.ResponseHandler
import com.github.kittinunf.fuel.moshi.defaultMoshi
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.github.kittinunf.fuel.moshi.responseObject
import com.github.kittinunf.fuel.test.MockHttpTestCase
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.Result.Failure
import com.github.kittinunf.result.Result.Success
import com.google.common.reflect.TypeToken
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.isA
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThat
import org.junit.Test
import java.net.HttpURLConnection

class FuelMoshiTest : MockHttpTestCase() {

    data class HttpBinUserAgentModel(var userAgent: String = "")

    @Test
    fun moshiTestResponseObject() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        Fuel.get(mock.path("user-agent"))
                .responseObject(moshiDeserializerOf(HttpBinUserAgentModel::class.java)) { _, _, result ->
                    assertThat(result.component1(), notNullValue())
                    assertThat(result.component2(), notNullValue())
                }
    }

    @Test
    fun moshiTestResponseObjectError() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        Fuel.get(mock.path("user-agent")).responseObject(moshiDeserializerOf(HttpBinUserAgentModel::class.java)) { _, _, result ->
            assertThat(result.component1(), notNullValue())
            assertThat(result.component2(), instanceOf(Result.Failure::class.java))
        }
    }

    @Test
    fun moshiTestResponseDeserializerObject() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        Fuel.get(mock.path("user-agent")).responseObject<HttpBinUserAgentModel> { _, _, result ->
            assertThat(result.component1(), notNullValue())
            assertThat(result.component2(), notNullValue())
        }
    }

    @Test
    fun moshiTestResponseDeserializerObjectError() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        Fuel.get(mock.path("user-agent")).responseObject<HttpBinUserAgentModel> { _, _, result ->
            assertThat(result.component1(), notNullValue())
            assertThat(result.component2(), instanceOf(Result.Failure::class.java))
        }
    }

    @Test
    fun moshiTestResponseHandlerObject() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        Fuel.get(mock.path("user-agent")).responseObject(object : ResponseHandler<HttpBinUserAgentModel> {
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
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        Fuel.get(mock.path("user-agent")).responseObject(object : ResponseHandler<HttpBinUserAgentModel> {
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
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        val triple = Fuel.get(mock.path("user-agent")).responseObject<HttpBinUserAgentModel>()
        assertThat(triple.third.component1(), notNullValue())
        assertThat(triple.third.component1(), instanceOf(HttpBinUserAgentModel::class.java))
    }

    @Test
    fun moshiTestResponseSyncObjectError() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        val triple = Fuel.get(mock.path("user-agent")).responseObject<HttpBinUserAgentModel>()
        assertThat(triple.third.component2(), instanceOf(FuelError::class.java))
    }

    @Test
    fun moshiTestResponseObjectErrorWithGivenAdapter() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(HttpBinUserAgentModel::class.java)

        Fuel.get(mock.path("user-agent")).responseObject(moshiDeserializerOf(adapter)) { _, _, result ->
            assertThat(result.component1(), notNullValue())
            assertThat(result.component2(), notNullValue())
        }
    }

    data class IssueInfo(val id: Int, val title: String, val number: Int)

    /**
     * Test for https://github.com/kittinunf/Fuel/issues/233
     */
    @Test
    fun moshiTestProcessingGenericList() {
        mock.chain(
            request = mock.request().withPath("/issues"),
            response = mock.response().withBody("[ " +
                    "{ \"id\": 1, \"title\": \"issue 1\", \"number\": null }, " +
                    "{ \"id\": 2, \"title\": \"issue 2\", \"number\": 32 }, " +
                    " ]").withStatusCode(HttpURLConnection.HTTP_OK)
        )

        Fuel.get(mock.path("issues")).responseObject<List<IssueInfo>> { _, _, result ->
            val issues = result.get()
            assertNotEquals(issues.size, 0)
            assertThat(issues[0], instanceOf(IssueInfo::class.java))
        }
    }

    enum class Stage {
        @Json(name = "na")
        UNKNOWN,
        IN_PROGRESS,
        FINISHED
    }

    data class StageDTO(val stage: Stage)

    class StageMoshiAdapter : JsonAdapter<Stage>() {
        override fun fromJson(reader: JsonReader): Stage? {
            val value = reader.nextString()

            return when (value) {
                "na" -> Stage.UNKNOWN
                "in_progress" -> Stage.IN_PROGRESS
                "finished" -> Stage.FINISHED
                else -> error("No supported value")
            }
        }

        override fun toJson(writer: JsonWriter, value: Stage?) {
        }
    }

    @Test
    fun moshiTestCustomAdapterSuccess() {
        defaultMoshi.add(TypeToken.of(Stage::class.java).type, StageMoshiAdapter())

        mock.apply {
            chain(
                request = mock.request().withPath("/stage1"),
                response = mock.response().withBody(""" { "stage" : "na" } """.trimIndent())
            )
            chain(
                request = mock.request().withPath("/stage2"),
                response = mock.response().withBody(""" { "stage" : "in_progress" } """.trimIndent())
            )
            chain(
                request = mock.request().withPath("/stage3"),
                response = mock.response().withBody(""" { "stage" : "finished" } """.trimIndent())
            )
        }

        val (req, res, res1) = Fuel.get(mock.path("stage1")).responseObject<StageDTO>()

        assertThat(req, notNullValue())
        assertThat(res, notNullValue())
        assertThat(res1 as Success, isA(Result.Success::class.java))
        assertThat(res1.value.stage, equalTo(Stage.UNKNOWN))

        val (_, _, res2) = Fuel.get(mock.path("stage2")).responseObject<StageDTO>()

        assertThat(res2 as Success, isA(Result.Success::class.java))
        assertThat(res2.value.stage, equalTo(Stage.IN_PROGRESS))

        val (_, _, res3) = Fuel.get(mock.path("stage3")).responseObject<StageDTO>()

        assertThat(res3 as Success, isA(Result.Success::class.java))
        assertThat(res3.value.stage, equalTo(Stage.FINISHED))
    }

    @Test
    fun moshiTestCustomAdapterFailure() {
        defaultMoshi.add(TypeToken.of(Stage::class.java).type, StageMoshiAdapter())

        mock.apply {
            chain(
                request = mock.request().withPath("/stage-error"),
                response = mock.response().withBody(""" { "stage" : "abcdef" } """.trimIndent())
            )
        }

        val (req, res, res1) = Fuel.get(mock.path("stage-error")).responseObject<StageDTO>()

        assertThat(req, notNullValue())
        assertThat(res, notNullValue())
        assertThat(res1 as Failure, isA(Result.Failure::class.java))
        assertThat(res1.error.exception as IllegalStateException, isA(IllegalStateException::class.java))
    }
}
