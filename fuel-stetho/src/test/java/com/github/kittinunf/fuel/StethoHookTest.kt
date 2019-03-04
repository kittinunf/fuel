package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.stetho.StethoHook
import com.github.kittinunf.fuel.test.MockHttpTestCase
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.URL

class StethoHookTest : MockHttpTestCase() {

    private val hook = StethoHook()

    @Test
    fun hookName() {
        assertThat(hook.friendlyName, equalTo("StethoFuelConnectionManager"))
    }

    @Test
    fun stethoConnectionIsCreated() {
        val cache = hook.stethoCache

        assertThat(cache.size, equalTo(0))

        hook.preConnect(URL("http://foo.bar").openConnection() as HttpURLConnection, mock.path("").httpGet())

        assertThat(cache.size, equalTo(1))
    }

    @Test
    fun stethoLifecycleIsCallingCorrectly() {
        val cache = hook.stethoCache

        val r1 = mock.path("").httpGet()
        val r2 = mock.path("").httpPut()
        val r3 = mock.path("").httpDelete()

        hook.preConnect(URL("http://foo.bar").openConnection() as HttpURLConnection, r1)
        hook.preConnect(URL("http://foo.bar").openConnection() as HttpURLConnection, r2)
        hook.preConnect(URL("http://foo.bar").openConnection() as HttpURLConnection, r3)

        hook.postConnect(r2)
        hook.postConnect(r3)
        hook.postConnect(r1)

        hook.interpretResponseStream(r3, ByteArrayInputStream(byteArrayOf()))
        hook.interpretResponseStream(r2, ByteArrayInputStream(byteArrayOf()))
        hook.interpretResponseStream(r1, ByteArrayInputStream(byteArrayOf()))

        assertThat(cache.size, equalTo(0))
    }
}
