package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.stetho.StethoHook
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class StethoHookTest {

    private val hook = StethoHook()

    @Test
    fun hookName() {
        assertThat(hook.friendlyName, equalTo("StethoFuelConnectionManager"))
    }

    @Test
    fun underlyObjectNotNull() {
        assertThat(hook.stetho, notNullValue())
    }
}
