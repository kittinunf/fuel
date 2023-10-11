package com.github.kittinunf.fuel.core

import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.io.File

class DataPartTest {
    private val currentDir = File(System.getProperty("user.dir"), "src/test/assets")
    val shortFile = File(currentDir, "lorem_ipsum_short.tmp")
    val file = File(currentDir, "lorem_ipsum_short.tmp")

    @Test
    fun escapeContentDispositionFileName() {
        val filename = "malicious.sh\";\r\ndummy=a.txt"
        val expectEscapedFilename = "malicious.sh%22;%0D%0Adummy=a.txt"

        val specialCharInlinePart = InlineDataPart("first", name = "first", contentType = "application/json", filename = filename)
        val specialCharFilePart = FileDataPart(shortFile, name = "second", filename = filename)
        val specialCharBlobPart = BlobDataPart(file.inputStream(), name = "third", contentLength = file.length(), filename = filename)

        assertThat(
                "ContentDisposition filename must escape Double-Quote and CRLF",
                specialCharInlinePart.contentDisposition == "form-data; name=\"first\"; filename=\"$expectEscapedFilename\""
        )
        assertThat(
                "ContentDisposition filename must escape Double-Quote and CRLF",
                specialCharFilePart.contentDisposition == "form-data; name=\"second\"; filename=\"$expectEscapedFilename\""
        )
        assertThat(
                "ContentDisposition filename must escape Double-Quote and CRLF",
                specialCharBlobPart.contentDisposition == "form-data; name=\"third\"; filename=\"$expectEscapedFilename\""
        )
    }

    @Test
    fun escapeNotContainSpecialChar() {
        val normalFileName = "abc.txt"
        val normalCharInlinePart = InlineDataPart("first", name = "first", contentType = "application/json", filename = normalFileName)
        val normalFilePart = FileDataPart(shortFile, name = "second", filename = normalFileName)
        val normalBlobPart = BlobDataPart(file.inputStream(), name = "third", contentLength = file.length(), filename = normalFileName)

        assertThat(
                "filename should be output as is if it does not contain special characters(Double-Quote, CRLF)",
                normalCharInlinePart.contentDisposition == "form-data; name=\"first\"; filename=\"abc.txt\""
        )
        assertThat(
                "filename should be output as is if it does not contain special characters(Double-Quote, CRLF)",
                normalFilePart.contentDisposition == "form-data; name=\"second\"; filename=\"abc.txt\""
        )
        assertThat(
                "filename should be output as is if it does not contain special characters(Double-Quote, CRLF)",
                normalBlobPart.contentDisposition == "form-data; name=\"third\"; filename=\"abc.txt\""
        )
    }
}