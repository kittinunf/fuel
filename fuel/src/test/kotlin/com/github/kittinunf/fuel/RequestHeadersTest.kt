package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.RegularRequest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.fail
import org.junit.Test
import java.net.URL
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class RequestHeadersTest : BaseTestCase() {
    @Test
    fun requestHeadersFromReadmeIntegrity() {
        val request = RegularRequest(method = Method.POST, path = "/my-post-path", url = URL("https://test.fuel.com/my-post-path"))
            .header(Headers.ACCEPT, "text/html, */*; q=0.1")
            .header(Headers.CONTENT_TYPE, "image/png")
            .header(Headers.COOKIE to "basic=very")
            .appendHeader(Headers.COOKIE to "value_1=foo", Headers.COOKIE to "value_2=bar", Headers.ACCEPT to "application/json")
            .appendHeader("MyFoo" to "bar", "MyFoo" to "baz")

        val recordedSets = mutableMapOf<String, String>()

        request.headers.transformIterate(
            { key: String, value: String -> recordedSets.put(key, value) },
            { k, v -> fail("Expected only header `set` and `add` with $k and $v") }
        )

        assertThat(recordedSets[Headers.ACCEPT], isEqualTo("text/html, */*; q=0.1, application/json"))
        assertThat(recordedSets[Headers.CONTENT_TYPE], isEqualTo("image/png"))
        assertThat(recordedSets[Headers.COOKIE], isEqualTo("basic=very; value_1=foo; value_2=bar"))
        assertThat(recordedSets["MyFoo"], isEqualTo("bar, baz"))
    }

    @Test
    fun requestHasHeaderGetter() {
        val request = RegularRequest(method = Method.GET, path = "/", url = URL("https://test.fuel.com/"))
            .header(Headers.CONTENT_TYPE, "text/html")

        val values = request[Headers.CONTENT_TYPE]
        assertThat(values, isEqualTo(request.header(Headers.CONTENT_TYPE)))
        assertThat(values.lastOrNull(), isEqualTo("text/html"))
    }

    @Test
    fun requestHasHeaderSetter() {
        val request = RegularRequest(method = Method.GET, path = "/", url = URL("https://test.fuel.com/"))
        request[Headers.CONTENT_TYPE] = "text/html"

        val values = request.header(Headers.CONTENT_TYPE)
        assertThat(values.lastOrNull(), isEqualTo("text/html"))
        assertThat(values.size, isEqualTo(1))
    }

    @Test
    fun setHeaderSetterSingleReplacesOldValue() {
        val request = RegularRequest(method = Method.GET, path = "/", url = URL("https://test.fuel.com/"))
            .header(Headers.CONTENT_TYPE, "text/html")

        request[Headers.CONTENT_TYPE] = "text/plain"

        val values = request.header(Headers.CONTENT_TYPE)
        assertThat(values.lastOrNull(), isEqualTo("text/plain"))
        assertThat(values.size, isEqualTo(1))
    }

    @Test
    fun setHeaderFunctionSingleReplacesOldValue() {
        val request = RegularRequest(method = Method.GET, path = "/", url = URL("https://test.fuel.com/"))
            .header(Headers.CONTENT_TYPE, "text/html")

        request.header(Headers.CONTENT_TYPE, "application/json")

        val values = request.header(Headers.CONTENT_TYPE)
        assertThat(values.lastOrNull(), isEqualTo("application/json"))
        assertThat(values.size, isEqualTo(1))
    }

    @Test
    fun setHeaderSetterMultipleReplacesOldValue() {
        val request = RegularRequest(method = Method.GET, path = "/", url = URL("https://test.fuel.com/"))
            .header(Headers.ACCEPT, "text/html")

        request[Headers.ACCEPT] = listOf("text/plain", "*/*; q=0.2")

        val values = request.header(Headers.ACCEPT)
        assertThat(values.firstOrNull(), isEqualTo("text/plain"))
        assertThat(values.lastOrNull(), isEqualTo("*/*; q=0.2"))
        assertThat(values.size, isEqualTo(2))
    }

    @Test
    fun setHeaderFunctionMultipleReplacesOldValue() {
        val request = RegularRequest(method = Method.GET, path = "/", url = URL("https://test.fuel.com/"))
            .header(Headers.ACCEPT, "text/html")

        request.header(Headers.ACCEPT, listOf("text/plain", "*/*; q=0.2"))

        val values = request.header(Headers.ACCEPT)
        assertThat(values.firstOrNull(), isEqualTo("text/plain"))
        assertThat(values.lastOrNull(), isEqualTo("*/*; q=0.2"))
        assertThat(values.size, isEqualTo(2))
    }

    @Test
    fun setHeaderFunctionMapReplacesOldValue() {
        val request = RegularRequest(method = Method.GET, path = "/", url = URL("https://test.fuel.com/"))
            .header(Headers.ACCEPT, "text/html")

        request.header(mapOf(Headers.ACCEPT to listOf("text/plain", "*/*; q=0.2")))

        val values = request.header(Headers.ACCEPT)
        assertThat(values.firstOrNull(), isEqualTo("text/plain"))
        assertThat(values.lastOrNull(), isEqualTo("*/*; q=0.2"))
        assertThat(values.size, isEqualTo(2))
    }

    @Test
    fun setHeaderFunctionPairsReplacesOldValue() {
        val request = RegularRequest(method = Method.GET, path = "/", url = URL("https://test.fuel.com/"))
                .header(Headers.ACCEPT, "text/html")

        request.header(Headers.ACCEPT to listOf("text/plain", "*/*; q=0.2"))

        val values = request.header(Headers.ACCEPT)
        assertThat(values.firstOrNull(), isEqualTo("text/plain"))
        assertThat(values.lastOrNull(), isEqualTo("*/*; q=0.2"))
        assertThat(values.size, isEqualTo(2))
    }

    @Test
    fun setHeaderFunctionVarArgsReplacesOldValue() {
        val request = RegularRequest(method = Method.GET, path = "/", url = URL("https://test.fuel.com/"))
                .header(Headers.ACCEPT, "text/html")

        request.header(Headers.ACCEPT, "text/plain", "*/*; q=0.2")

        val values = request.header(Headers.ACCEPT)
        assertThat(values.firstOrNull(), isEqualTo("text/plain"))
        assertThat(values.lastOrNull(), isEqualTo("*/*; q=0.2"))
        assertThat(values.size, isEqualTo(2))
    }

    @Test
    fun setHeaderWithPairsPreservesDuplicateKeys() {
        val request = RegularRequest(method = Method.GET, path = "/", url = URL("https://test.fuel.com/"))
                .header(Headers.ACCEPT_ENCODING, "identity")

        request.header(Headers.ACCEPT_ENCODING to "br", Headers.ACCEPT_ENCODING to "gzip")
        val values = request.header(Headers.ACCEPT_ENCODING)
        assertThat(values.firstOrNull(), isEqualTo("br"))
        assertThat(values.lastOrNull(), isEqualTo("gzip"))
        assertThat(values.size, isEqualTo(2))
    }

    @Test
    fun setHeaderWithMapTakesLastKey() {
        val request = RegularRequest(method = Method.GET, path = "/", url = URL("https://test.fuel.com/"))
                .header(Headers.ACCEPT_ENCODING, "identity")

        request.header(mapOf(Headers.ACCEPT_ENCODING to "br", Headers.ACCEPT_ENCODING to "gzip"))

        val values = request.header(Headers.ACCEPT_ENCODING)
        assertThat(values.firstOrNull(), isEqualTo("gzip"))
        assertThat(values.size, isEqualTo(1))
    }

    @Test
    fun appendHeader() {
        val request = RegularRequest(method = Method.GET, path = "/", url = URL("https://test.fuel.com/"))
            .header(Headers.ACCEPT_LANGUAGE, "en-US", "nl-NL; q=0.9")

        request.appendHeader(Headers.ACCEPT_LANGUAGE, "fr-FR; q=0.8")

        val values = request.header(Headers.ACCEPT_LANGUAGE)
        assertThat(values.firstOrNull(), isEqualTo("en-US"))
        assertThat(values.lastOrNull(), isEqualTo("fr-FR; q=0.8"))
        assertThat(values.size, isEqualTo(3))
    }

    @Test
    fun appendHeaderWithVarArgs() {
        val request = RegularRequest(method = Method.GET, path = "/", url = URL("https://test.fuel.com/"))
                .header(Headers.ACCEPT_LANGUAGE, "en-US")

        request.appendHeader(Headers.ACCEPT_LANGUAGE, "nl-NL; q=0.9", "fr-FR; q=0.8")

        val values = request.header(Headers.ACCEPT_LANGUAGE)
        assertThat(values.firstOrNull(), isEqualTo("en-US"))
        assertThat(values.lastOrNull(), isEqualTo("fr-FR; q=0.8"))
        assertThat(values.size, isEqualTo(3))
    }

    @Test
    fun appendHeaderWithPairs() {
        val request = RegularRequest(method = Method.GET, path = "/", url = URL("https://test.fuel.com/"))
                .header(Headers.ACCEPT_LANGUAGE, "en-US")

        request.appendHeader(Headers.ACCEPT_LANGUAGE to "nl-NL; q=0.9", Headers.ACCEPT_LANGUAGE to "fr-FR; q=0.8")

        val values = request.header(Headers.ACCEPT_LANGUAGE)
        assertThat(values.firstOrNull(), isEqualTo("en-US"))
        assertThat(values.lastOrNull(), isEqualTo("fr-FR; q=0.8"))
        assertThat(values.size, isEqualTo(3))
    }
}