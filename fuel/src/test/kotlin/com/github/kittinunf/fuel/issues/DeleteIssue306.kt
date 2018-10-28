package com.github.kittinunf.fuel.issues

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.MockHttpTestCase
import com.github.kittinunf.fuel.MockReflected
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockserver.model.Header.header

class DeleteIssue306 : MockHttpTestCase() {

    companion object {
        const val PERSISTENT_MENU = "persistent_menu"
    }

    @Test
    fun itCorrectlySendsTheBody() {
        val version = 3
        val uri = "$version/me/messenger_profile"

        val root = "{ \"fields\": [\"$PERSISTENT_MENU\"] }"

        val request = Fuel.delete(mock.path(uri), parameters = listOf("access_token" to "730161810405329|J5eZMzywkpHjjeQKtpbgN-Eq0tQ"))
            .header(mapOf(Headers.CONTENT_TYPE to "application/json"))
            .body(root)

        mock.chain(
            request = mock.request()
                .withMethod(Method.DELETE.value)
                .withPath("/$uri")
                .withHeader(header(Headers.CONTENT_TYPE, "application/json")),
            response = mock.reflect()
        )

        val (_, _, result) = request.responseObject(MockReflected.Deserializer())
        val (reflected, error) = result

        assertThat(error, nullValue())
        assertThat(reflected, notNullValue())
        assertThat(reflected!!.body?.string ?: "(no body)", equalTo(root))
    }
}

