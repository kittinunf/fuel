package com.github.kittinunf.fuel.livedata

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.result.Result
import org.hamcrest.CoreMatchers
import org.junit.Assert
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
                    Assert.assertThat(it?.first, CoreMatchers.notNullValue())
                    Assert.assertThat(it?.second, CoreMatchers.notNullValue())
                }
    }

    @Test
    fun liveDataTestResponseString() {
        Fuel.get("/get").liveDataResponseString()
                .observeForever {
                    Assert.assertThat(it?.first, CoreMatchers.notNullValue())
                    Assert.assertThat(it?.second, CoreMatchers.notNullValue())
                }
    }

    @Test
    fun liveDataTestString() {
        Fuel.get("/get").liveDataString()
                .observeForever {
                    Assert.assertThat(it, CoreMatchers.notNullValue())
                }
    }

    @Test
    fun liveTestStringError() {
        Fuel.get("/gt").liveDataString()
                .observeForever {
                    assert(it is Result.Failure)
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
                    Assert.assertThat(it?.first, CoreMatchers.notNullValue())
                    Assert.assertThat(it?.second, CoreMatchers.notNullValue())
                }
    }

    @Test
    fun liveDataTestResponseObjectError() {
        Fuel.get("/useragent")
                .liveDataResponseObject(HttpBinUserAgentModelDeserializer())
                .observeForever {
                    Assert.assertThat(it?.first, CoreMatchers.notNullValue())
                    assert(it?.second is Result.Failure)
                }
    }

}