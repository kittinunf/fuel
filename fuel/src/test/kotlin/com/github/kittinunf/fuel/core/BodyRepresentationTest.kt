package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.core.requests.DefaultBody
import com.github.kittinunf.fuel.test.MockHttpTestCase
import com.github.kittinunf.fuel.util.decodeBase64
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockserver.model.BinaryBody
import java.io.ByteArrayInputStream
import java.net.URLConnection
import java.util.Random

class BodyRepresentationTest : MockHttpTestCase() {
    private val manager: FuelManager by lazy { FuelManager() }

    @Test
    fun emptyBodyRepresentation() {
        assertThat(
            DefaultBody.from({ ByteArrayInputStream(ByteArray(0)) }, { 0L }).asString("(unknown)"),
            equalTo("(empty)")
        )
    }

    @Test
    fun unknownBytesRepresentation() {
        val bytes = ByteArray(555 - 16)
            .also { Random().nextBytes(it) }
            .let { ByteArray(16).plus(it) }

        mock.chain(
            request = mock.request().withMethod(Method.GET.value).withPath("/bytes"),
            response = mock.response().withBody(BinaryBody(bytes, null)).withHeader("Content-Type", "")
        )

        val (_, response, _) = manager.request(Method.GET, mock.path("bytes")).responseString()
        assertThat(
            response.body().asString(response[Headers.CONTENT_TYPE].lastOrNull()),
            equalTo("(555 bytes of (unknown))")
        )
    }

    @Test
    fun guessContentType() {
        val decodedImage = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVQYV2NgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII=".decodeBase64()!!

        assertThat(
            DefaultBody
                .from({ ByteArrayInputStream(decodedImage) }, { decodedImage.size.toLong() })
                .asString(URLConnection.guessContentTypeFromStream(ByteArrayInputStream(decodedImage))),
            equalTo("(${decodedImage.size} bytes of image/png)")
        )
    }

    @Test
    fun bytesRepresentationOfOctetStream() {
        val contentTypes = listOf("application/octet-stream")
        val content = ByteArray(555 - 16)
            .also { Random().nextBytes(it) }
            .let { ByteArray(16).plus(it) }

        contentTypes.forEach { contentType ->
            assertThat(
                DefaultBody
                    .from({ ByteArrayInputStream(content) }, { content.size.toLong() })
                    .asString(contentType),
                equalTo("(555 bytes of $contentType)")
            )
        }
    }

    @Test
    fun bytesRepresentationOfMedia() {
        val contentTypes = listOf(
            "image/bmp", "image/gif", "image/jpeg", "image/png", "image/tiff", "image/webp", "image/x-icon",
            "audio/aac", "audio/midi", "audio/x-midi", "audio/ogg", "audio/wav", "audio/webm", "audio/3gpp", "audio/3gpp2",
            "video/mpeg", "video/ogg", "video/webm", "video/x-msvideo", "video/3gpp", "video/3gpp2",
            "font/otf", "font/ttf", "font/woff", "font/woff2"
        )
        val content = ByteArray(555 - 16)
            .also { Random().nextBytes(it) }
            .let { ByteArray(16).plus(it) }

        contentTypes.forEach { contentType ->
            assertThat(
                DefaultBody
                    .from({ ByteArrayInputStream(content) }, { content.size.toLong() })
                    .asString(contentType),
                equalTo("(555 bytes of $contentType)")
            )
        }
    }

    @Test
    fun textRepresentationOfYaml() {
        val contentTypes = listOf("application/x-yaml", "text/yaml")
        val content = "language: c\n"

        contentTypes.forEach { contentType ->
            assertThat(
                DefaultBody
                    .from({ ByteArrayInputStream(content.toByteArray()) }, { content.length.toLong() })
                    .asString(contentType),
                equalTo(content)
            )
        }
    }

    @Test
    fun textRepresentationOfXml() {
        val contentTypes = listOf("application/xml", "application/xhtml+xml", "application/vnd.fuel.test+xml", "image/svg+xml")
        val content = "<html xmlns=\"http://www.w3.org/1999/xhtml\"/>"

        contentTypes.forEach { contentType ->
            assertThat(
                DefaultBody
                    .from({ ByteArrayInputStream(content.toByteArray()) }, { content.length.toLong() })
                    .asString(contentType),
                equalTo(content)
            )
        }
    }

    @Test
    fun textRepresentationOfScripts() {
        val contentTypes = listOf("application/javascript", "application/typescript", "application/vnd.coffeescript")
        val content = "function test()"

        contentTypes.forEach { contentType ->
            assertThat(
                DefaultBody
                    .from({ ByteArrayInputStream(content.toByteArray()) }, { content.length.toLong() })
                    .asString(contentType),
                equalTo(content)
            )
        }
    }

    @Test
    fun textRepresentationOfJson() {
        val contentTypes = listOf("application/json")
        val content = "{ \"foo\": 42 }"

        contentTypes.forEach { contentType ->
            assertThat(
                DefaultBody
                    .from({ ByteArrayInputStream(content.toByteArray()) }, { content.length.toLong() })
                    .asString(contentType),
                equalTo(content)
            )
        }
    }

    @Test
    fun textRepresentationOfJsonWithMultibyteCharacterSets() {
        val contentTypes = listOf("application/json")
        val content = "{ \"foo\": \"４２\" }"

        contentTypes.forEach { contentType ->
            assertThat(
                DefaultBody
                    .from({ ByteArrayInputStream(content.toByteArray()) }, { content.length.toLong() })
                    .asString(),
                equalTo(content)
            )
        }
    }

    @Test
    fun textRepresentationOfJsonWithUtf8Charset() {
        val contentTypes = listOf("application/json;charset=utf-8", "application/json; charset=utf-8")
        val content = "{ \"foo\": 42 }"

        contentTypes.forEach { contentType ->
            assertThat(
                DefaultBody
                    .from({ ByteArrayInputStream(content.toByteArray()) }, { content.length.toLong() })
                    .asString(contentType),
                equalTo(content)
            )
        }
    }

    @Test
    fun textRepresentationOfJsonWithUtf8AndOtherParameters() {
        val contentTypes = listOf(
                "application/json;charset=utf-8;api-version=5.1",
                "application/json; charset=utf-8; api-version=5.1",
                "application/json;api-version=5.1;charset=utf-8",
                "application/json; api-version=5.1; charset=utf-8",
                "application/json;test=true;charset=utf-8;api-version=5.1",
                "application/json; test=true; charset=utf-8; api-version=5.1"
        )
        val content = "{ \"foo\": 42 }"

        contentTypes.forEach { contentType ->
            assertThat(
                    DefaultBody
                            .from({ ByteArrayInputStream(content.toByteArray()) }, { content.length.toLong() })
                            .asString(contentType),
                    equalTo(content)
            )
        }
    }

    @Test
    fun textRepresentationOfJsonWithDifferentCharsets() {
        val contentString = "{ \"foo\": 42 }"

        val contentMap = mapOf(
                "application/json; charset=utf-8" to Charsets.UTF_8,
                "application/json; charset=utf-16" to Charsets.UTF_16,
                "application/json; charset=utf-32" to Charsets.UTF_32,
                "application/json; charset=iso-8859-1" to Charsets.ISO_8859_1,
                "application/json; charset=ascii" to Charsets.US_ASCII
        )

        contentMap.forEach { (contentType, charset) ->
            assertThat(
                    DefaultBody
                            .from({ ByteArrayInputStream(contentString.toByteArray(charset)) }, { contentString.length.toLong() })
                            .asString(contentType),
                    equalTo(contentString)
            )
        }
    }

    @Test
    fun textRepresentationOfJsonWithoutCharset() {
        val contentTypes = listOf(
                "application/json;api-version=5.1",
                "application/json; api-version=5.1"
        )
        val content = "{ \"foo\": 42 }"

        contentTypes.forEach { contentType ->
            assertThat(
                    DefaultBody
                            .from({ ByteArrayInputStream(content.toByteArray()) }, { content.length.toLong() })
                            .asString(contentType),
                    equalTo(content)
            )
        }
    }

    @Test
    fun textRepresentationOfCsv() {
        val contentTypes = listOf("text/csv")
        val content = "function test()"

        contentTypes.forEach { contentType ->
            assertThat(
                DefaultBody
                    .from({ ByteArrayInputStream(content.toByteArray()) }, { content.length.toLong() })
                    .asString(contentType),
                equalTo(content)
            )
        }
    }

    @Test
    fun textRepresentationOfCsvWithUtf16beCharset() {
        val contentTypes = listOf("application/csv; charset=utf-16be", "application/csv;charset=utf-16be")
        val content = String("hello,world!".toByteArray(Charsets.UTF_16BE))

        contentTypes.forEach { contentType ->
            assertThat(
                DefaultBody
                    .from({ ByteArrayInputStream(content.toByteArray()) }, { content.length.toLong() })
                    .asString(contentType),
                equalTo("hello,world!")
            )
        }
    }

    @Test
    fun textRepresentationOfTextTypes() {
        val contentTypes = listOf("text/csv", "text/html", "text/calendar", "text/plain", "text/css")
        val content = "maybe invalid but we don't care"

        contentTypes.forEach { contentType ->
            assertThat(
                DefaultBody
                    .from({ ByteArrayInputStream(content.toByteArray()) }, { content.length.toLong() })
                    .asString(contentType),
                equalTo(content)
            )
        }
    }
}
