package com.github.kittinunf.fuel.livedata

import android.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.test.MockHttpTestCase
import com.github.kittinunf.result.Result
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.Test
import java.net.HttpURLConnection

/**
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class FuelLiveDataTest : MockHttpTestCase() {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    @Test
    fun liveDataTestResponse() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        Fuel.get(mock.path("user-agent")).liveDataResponse()
            .observeForever {
                assertThat(it?.first, notNullValue())
                assertThat(it?.second, notNullValue())
            }
    }

    @Test
    fun liveDataTestResponseString() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        Fuel.get(mock.path("user-agent")).liveDataResponseString()
            .observeForever {
                assertThat(it?.first, notNullValue())
                assertThat(it?.second, notNullValue())
            }
    }

    @Test
    fun liveDataTestString() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        Fuel.get(mock.path("user-agent")).liveDataString()
            .observeForever {
                assertThat(it, notNullValue())
            }
    }

    @Test
    fun liveTestStringError() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        Fuel.get(mock.path("user-agent")).liveDataString()
            .observeForever {
                assertThat(it, instanceOf(Result.Failure::class.java))
            }
    }

    // Model
    data class HttpBinUserAgentModel(var userAgent: String = "")

    // Deserializer
    class HttpBinUserAgentModelDeserializer : ResponseDeserializable<HttpBinUserAgentModel> {

        override fun deserialize(content: String): HttpBinUserAgentModel {
            return HttpBinUserAgentModel(content)
        }
    }

    @Test
    fun liveDataTestResponseObject() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.reflect()
        )

        Fuel.get(mock.path("user-agent"))
            .liveDataResponseObject(HttpBinUserAgentModelDeserializer())
            .observeForever {
                assertThat(it?.first, notNullValue())
                assertThat(it?.second, notNullValue())
            }
    }

    @Test
    fun liveDataTestResponseObjectError() {
        mock.chain(
            request = mock.request().withPath("/user-agent"),
            response = mock.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
        )

        Fuel.get(mock.path("user-agent"))
            .liveDataResponseObject(HttpBinUserAgentModelDeserializer())
            .observeForever {
                assertThat(it?.first, notNullValue())
                assertThat(it?.second, instanceOf(Result.Failure::class.java))
            }
    }
}