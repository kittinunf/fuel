package com.github.kittinunf.fuel

import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class MainTest {

    @Test
    fun test() {
        val (_, _, result) = Fuel.get("https://httpbin.org/get").responseString()
        println(result)
        assertThat(result.get(), containsString("\"url\": \"https://httpbin.org/get\""))
    }
}
