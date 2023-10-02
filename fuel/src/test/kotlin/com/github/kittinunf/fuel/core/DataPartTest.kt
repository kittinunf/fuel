package com.github.kittinunf.fuel.core

import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.io.File

class DataPartTest {
    private val currentDir = File(System.getProperty("user.dir"), "src/test/assets")

    @Test
    fun escapeContentDispositionFileName() {
        val shortFile = File(currentDir, "lorem_ipsum_short.tmp")
        val file = File(currentDir, "lorem_ipsum_short.tmp")
        val filename = "malicious.sh\";\r\ndummy=a.txt"
        val exceptEscapedFilename = "malicious.sh%22;%0D%0Adummy=a.txt"

        val specialCharInlinePart = InlineDataPart("first", name = "first", contentType = "application/json", filename = filename)
        val specialCharFilePart = FileDataPart(shortFile, name = "second", filename = filename)
        val specialCharBlobPart = BlobDataPart(file.inputStream(), name = "third", contentLength = file.length(), filename = filename)

        assertThat(
                "ContentDisposition filename must escape Double-Quote and CRLF",
                specialCharInlinePart.contentDisposition.contains(exceptEscapedFilename)
        )
        assertThat(
                "ContentDisposition filename must escape Double-Quote and CRLF",
                specialCharFilePart.contentDisposition.contains(exceptEscapedFilename)
        )
        assertThat(
                "ContentDisposition filename must escape Double-Quote and CRLF",
                specialCharBlobPart.contentDisposition.contains(exceptEscapedFilename)
        )

        val normalFileName = "abc.txt"
        val normalCharInlinePart = InlineDataPart("first", name = "first", contentType = "application/json", filename = normalFileName)
        val normalFilePart = FileDataPart(shortFile, name = "second", filename = normalFileName)
        val normalBlobPart = BlobDataPart(file.inputStream(), name = "third", contentLength = file.length(), filename = normalFileName)

        assertThat(
                "filename should be output as is if it does not contain special characters(Double-Quote, CRLF)",
                normalCharInlinePart.contentDisposition.contains("abc.txt")
        )
        assertThat(
                "filename should be output as is if it does not contain special characters(Double-Quote, CRLF)",
                normalFilePart.contentDisposition.contains("abc.txt")
        )
        assertThat(
                "filename should be output as is if it does not contain special characters(Double-Quote, CRLF)",
                normalBlobPart.contentDisposition.contains("abc.txt")
        )
    }
}