package com.github.kittinunf.fuel.livedata

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.result.Result
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class FuelLiveDataTest {

    init {
        FuelManager.instance.basePath = "https://httpbin.org"

        Fuel.testMode {
            timeout = 15000
        }
    }

    @Test
    fun liveDataTestResponse() {
        Fuel.get("/get").liveDataResponse()
                .observeForever {
                    assertThat(it?.first, notNullValue())
                    assertThat(it?.second, notNullValue())
                }
    }

    @Test
    fun liveDataTestResponseString() {
        Fuel.get("/get").liveDataResponseString()
                .observeForever {
                    assertThat(it?.first, notNullValue())
                    assertThat(it?.second, notNullValue())
                }
    }

    @Test
    fun liveDataTestString() {
        Fuel.get("/get").liveDataString()
                .observeForever {
                    assertThat(it, notNullValue())
                }
    }

    @Test
    fun liveTestStringError() {
        Fuel.get("/gt").liveDataString()
                .observeForever {
                    assertThat(it, instanceOf(Result.Failure::class.java))
                }
    }

    //Model
    data class HttpBinUserAgentModel(var userAgent: String = "")

    //Deserializer
    class HttpBinUserAgentModelDeserializer : ResponseDeserializable<HttpBinUserAgentModel> {

        override fun deserialize(content: String): HttpBinUserAgentModel {
            return HttpBinUserAgentModel(content)
        }

    }

    @Test
    fun liveDataTestResponseObject() {
        Fuel.get("/user-agent")
                .liveDataResponseObject(HttpBinUserAgentModelDeserializer())
                .observeForever {
                    assertThat(it?.first, notNullValue())
                    assertThat(it?.second, notNullValue())
                }
    }

    @Test
    fun liveDataTestResponseObjectError() {
        Fuel.get("/useragent")
                .liveDataResponseObject(HttpBinUserAgentModelDeserializer())
                .observeForever {
                    assertThat(it?.first, notNullValue())
                    assertThat(it?.second, instanceOf(Result.Failure::class.java))
                }
    }

}