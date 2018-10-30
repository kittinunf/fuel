package com.github.kittinunf.fuel.issues

import com.github.kittinunf.fuel.MockHttpTestCase
import com.github.kittinunf.fuel.MockReflected
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

class BodyInterceptorIssue464 : MockHttpTestCase() {

    private val threadSafeManager = FuelManager()
    private val bodyInterceptor = { next: (Request) -> Request ->
        { request: Request ->
            val body = request.body.toByteArray()
            // make transformations based on the original request/body
            val transformed = request.body(body.reversedArray())
                    .header("Body-Interceptor", "Intercepted")
            next(transformed)
        }
    }

    @Before
    fun addBodyInterceptor() {
        threadSafeManager.addRequestInterceptor(bodyInterceptor)
    }

    @After
    fun removeBodyInterceptor() {
        threadSafeManager.removeRequestInterceptor(bodyInterceptor)
    }

    @Test
    fun getBodyInInterceptor() {
        val value = "foobarbaz"
        val request = reflectedRequest(Method.POST, "intercepted-body", manager = threadSafeManager)
            .body(value)

        val (_, _, result) = request.responseObject(MockReflected.Deserializer())
        val (reflected, error) = result

        assertThat(error, nullValue())
        assertThat(reflected, notNullValue())
        assertThat(reflected!!["Body-Interceptor"].firstOrNull(), equalTo("Intercepted"))
        assertThat(reflected.body?.string, equalTo(value.reversed()))
    }
}
