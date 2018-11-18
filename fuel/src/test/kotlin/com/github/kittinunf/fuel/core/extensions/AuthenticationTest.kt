package com.github.kittinunf.fuel.core.extensions

import com.github.kittinunf.fuel.core.requests.DefaultRequest
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.util.encodeBase64ToString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.isA
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.net.URL

class AuthenticationTest {
    @Test
    fun basicAuthentication() {
        val request = DefaultRequest(Method.GET, URL("https://test.fuel.com/authentication"))
            .authentication()
            .basic("username", "password")

        val encodedCredentials = "username:password".encodeBase64ToString()
        assertThat(request[Headers.AUTHORIZATION].lastOrNull(), equalTo("Basic $encodedCredentials"))
    }

    @Test
    fun bearerAuthentication() {
        val request = DefaultRequest(Method.GET, URL("https://test.fuel.com/authentication"))
            .authentication()
            .bearer("token")

        assertThat(request[Headers.AUTHORIZATION].lastOrNull(), equalTo("Bearer token"))
    }

    @Test
    fun authenticationFeatureActsLikeRequest() {
        val request = DefaultRequest(Method.GET, URL("https://test.fuel.com/authentication"))
        val requestWithAuthentication = request.authentication()

        assertThat(requestWithAuthentication, isA(Request::class.java))
        assertThat(requestWithAuthentication.request, equalTo(requestWithAuthentication))
    }
}