package com.github.kittinunf.fuel.toolbox.extensions

import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.test.MockHttpTestCase
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import java.net.HttpURLConnection
import java.net.URL

class ForceMethodTest : MockHttpTestCase() {
    @Test
    fun forceHttpPatchMethod() {
        val connection = URL(this.mock.path("force-patch-test")).openConnection()
        assert(connection is HttpURLConnection)
        val httpUrlConnection = connection as HttpURLConnection
        httpUrlConnection.forceMethod(Method.PATCH)
        assertThat(httpUrlConnection.requestMethod, equalTo(Method.PATCH.value))
    }

    @Test
    fun forceHttpPostMethod() {
        val connection = URL(this.mock.path("force-post-test")).openConnection()
        assert(connection is HttpURLConnection)
        val httpUrlConnection = connection as HttpURLConnection
        httpUrlConnection.forceMethod(Method.POST)
        assertThat(httpUrlConnection.requestMethod, equalTo(Method.POST.value))
    }
}