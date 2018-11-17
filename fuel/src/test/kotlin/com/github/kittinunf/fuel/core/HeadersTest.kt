package com.github.kittinunf.fuel.core

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class HeadersTest {

    @Test
    fun headerNameEqualityCaseInsensitive() {
        val left = HeaderName("my-case-insensitive-header")
        val right = HeaderName("My-Case-Insensitive-Header")

        assertThat(left, equalTo(left))
        assertThat(left, equalTo(right))
        assertThat(left, not(equalTo(HeaderName("OtherHeader"))))
    }

    @Test
    fun headerNamePreservedInputCase() {
        val input = "My-Case-Insensitive-Header"
        val headerName = HeaderName(input)

        assertThat(headerName.toString(), equalTo(input))
        assertThat(headerName.name, equalTo(input))
    }

    @Test
    fun containsKeyIsCaseInsensitive() {
        val headers = Headers()
        val header = "My-Case-Insensitive-Header"
        val value = "value"
        val testKey = header.toLowerCase()

        headers[header] = value

        assertThat("Expected $header to exist", headers.containsKey(header), equalTo(true))
        assertThat("Expected $testKey to exist", headers.containsKey(testKey), equalTo(true))
    }

    @Test
    fun setIsCaseInsensitive() {
        val headers = Headers()
        val header = "My-Case-Insensitive-Header"
        val originalValue = "value"
        val newValue = "new-value"
        val testKey = header.toLowerCase()

        headers[header] = originalValue
        headers[testKey] = newValue

        val values = headers[header]
        assertThat(values.first(), equalTo(newValue))
        assertThat(values.size, equalTo(1))
    }

    @Test
    fun getIsCaseInsensitive() {
        val headers = Headers()
        val header = "My-Case-Insensitive-Header"
        val value = "value"
        val testKey = header.toLowerCase()

        headers[header] = value

        assertThat(headers[header].first(), equalTo(value))
        assertThat(headers[testKey].first(), equalTo(value))
    }

    @Test
    fun setIsReplace() {
        val headers = Headers()
        val header = "My-Case-Insensitive-Header"
        val originalValue = "value"
        val newValue = "new-value"

        headers[header] = originalValue
        headers[header] = newValue

        val values = headers[header]
        assertThat(values.first(), equalTo(newValue))
        assertThat(values.size, equalTo(1))
    }

    @Test
    fun appendIsNotReplace() {
        val headers = Headers()
        val header = "My-Case-Insensitive-Header"
        val originalValue = "value"
        val newValue = "new-value"

        headers.append(header, originalValue)
        headers.append(header, newValue)

        val values = headers[header]
        assertThat(values.first(), equalTo(originalValue))
        assertThat(values.last(), equalTo(newValue))
        assertThat(values.size, equalTo(2))
    }

    @Test
    fun appendingSingleHeadersIsReplace() {
        val headers = Headers()
        val header = Headers.CONTENT_TYPE
        val originalValue = "application/json"
        val newValue = "test/html"

        headers.append(header, originalValue)
        headers.append(header, newValue)

        val values = headers[header]
        assertThat(values.last(), equalTo(newValue))
        assertThat(values.size, equalTo(1))
    }

    @Test
    fun removeIsCaseInsensitive() {
        val headers = Headers()
        val originalKey = "My-Case-Insensitive-Header"
        val value = "value"
        val testKey = originalKey.toLowerCase()

        headers[originalKey] = value
        headers.remove(testKey)

        val values = headers[originalKey]
        assertThat(values.size, equalTo(0))
    }

    @Test
    fun setCookieIsNotCollapsible() {
        // RFC exception
        assertThat(
            "Expected SET_COOKIE to not be collapsible",
            Headers.isCollapsible(Headers.SET_COOKIE),
            equalTo(false)
        )
    }

    @Test
    fun variousHeadersAreSingleValues() {
        val singles = listOf(
            Headers.AGE, Headers.CONTENT_TYPE, Headers.CONTENT_LENGTH, Headers.CONTENT_LOCATION, Headers.EXPECT,
            Headers.EXPIRES, Headers.LOCATION
        )

        // RFC specific, and source of a few bugs so test explicitly
        singles.forEach {
            assertThat(
                "Expected $it to be a single value",
                Headers.isSingleValue(it),
                equalTo(true)
            )
        }
    }

    @Test
    fun cookieCollapsesWithSemiColon() {
        val header = HeaderName(Headers.COOKIE)
        val values = listOf("foo=2", "bar=3")

        // RFC specific, Cookie collapses with a semi-colon
        val collapsed = Headers.collapse(header, values)
        assertThat(collapsed, equalTo(values.joinToString("; ")))
    }

    @Test
    fun headersCollapseWithComma() {
        val header = HeaderName(Headers.ACCEPT_ENCODING)
        val values = listOf("gzip", "identity; q=0.1")
        val collapsed = Headers.collapse(header, values)
        assertThat(collapsed, equalTo(values.joinToString(", ")))
    }

    @Test
    fun transformIterateCallsCorrectMethodBasedOnSpec() {
        val headers = Headers()

        headers[Headers.ACCEPT_ENCODING] = listOf("gzip", "identity; q=0.1")
        headers[Headers.CONTENT_TYPE] = listOf("application/json", "text/html")
        headers[Headers.SET_COOKIE] = listOf(
            "sessionid=38afes7a8; HttpOnly; Path=/",
            "id=a3fWa; Expires=Wed, 21 Oct 2015 07:28:00 GMT; Secure; HttpOnly"
        )

        val recordAdd = mutableMapOf<String, List<String>>()
        val recordSet = mutableMapOf<String, String>()

        headers.transformIterate(
            { k, v -> recordSet.put(k, v) },
            { k, v -> recordAdd.put(k, (recordAdd[k] ?: emptyList()).plus(v)) }
        )

        val setCookieAdds = recordAdd[Headers.SET_COOKIE]
        assertThat("Expected SET_COOKIE to be added via 'add'", setCookieAdds, notNullValue())
        assertThat(setCookieAdds!!.size, equalTo(2))

        val acceptEncodingSets = recordSet[Headers.ACCEPT_ENCODING]
        val expectedCollapsedAcceptEncoding = Headers.collapse(
            Headers.ACCEPT_ENCODING,
            headers[Headers.ACCEPT_ENCODING]
        )
        assertThat("Expected ACCEPT_ENCODING to be added via 'set'", acceptEncodingSets, notNullValue())
        assertThat(acceptEncodingSets, equalTo(expectedCollapsedAcceptEncoding))

        val contentTypeSets = recordSet[Headers.CONTENT_TYPE]
        val expectedContentType = headers[Headers.CONTENT_TYPE].last()

        assertThat("Expected CONTENT_TYPE to be added via 'set'", contentTypeSets, notNullValue())
        assertThat(contentTypeSets, equalTo(expectedContentType))

        assertThat(recordSet.size, equalTo(2))
        assertThat(recordAdd.size, equalTo(1))
    }
}