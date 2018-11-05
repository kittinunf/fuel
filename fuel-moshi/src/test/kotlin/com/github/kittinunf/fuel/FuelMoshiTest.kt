package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Handler
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.github.kittinunf.fuel.moshi.responseObject
import com.github.kittinunf.fuel.test.MockHelper
import com.github.kittinunf.result.Result
import com.squareup.moshi.Moshi
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.After
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection

class FuelMoshiTest {

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

        Fuel.get(mock.path("user-agent"))
            .responseObject<HttpBinUserAgentModel> { _, _, result ->
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

        Fuel.get(mock.path("user-agent"))
            .responseObject<HttpBinUserAgentModel> { _, _, result ->
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

        Fuel.get(mock.path("user-agent"))
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
        mock.chain(
                request = mock.request().withPath("/user-agent"),
                response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        Fuel.get(mock.path("user-agent"))
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

        Fuel.get(mock.path("user-agent"))
            .responseObject(moshiDeserializerOf(adapter)) { _, _, result ->
                assertThat(result.component1(), notNullValue())
                assertThat(result.component2(), notNullValue())
            }
    }

    data class IssueInfo(val id: Int, val title: String, val number: Int)

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

        Fuel.get(mock.path("issues")).responseObject<List<IssueInfo>> { _, _, result ->
            val issues = result.get()
            assertNotEquals(issues.size, 0)
            assertThat(issues[0], instanceOf(IssueInfo::class.java))
        }
    }
}
