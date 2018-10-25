package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.Blob
import com.github.kittinunf.fuel.core.Body
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.util.copyTo
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.Reader
import java.net.URL
import java.net.URLConnection

internal class UploadTaskRequest(request: Request) : TaskRequest(request) {
    var progressCallback: ((Long, Long) -> Unit)? = null
    lateinit var sourceCallback: (Request, URL) -> Iterable<Blob>

    fun stream(output: Boolean) : Any {
        var contentLength= 0L

        val stream = PipedOutputStream()
        stream.buffered().apply {
            request.parameters.forEach { (name, data) ->
                contentLength += write("--$boundary")
                contentLength += writeln()
                contentLength += write("Content-Disposition: form-data; name=\"$name\"")
                contentLength += writeln()
                contentLength += write("Content-Type: text/plain")
                contentLength += writeln()
                contentLength += writeln()
                contentLength += write(data.toString())
                contentLength += writeln()
            }

            val files = sourceCallback(request, request.url)
            // need to convert to double to not lose anything between int and long
            val filesLength = contentLength + files.sumByDouble { it.length.toDouble() }.toLong()

            files.forEachIndexed { i, (name, length, inputStream) ->
                val postFix = if (files.count() == 1) "" else "${i + 1}"
                val fieldName = request.names.getOrElse(i) { request.name + postFix }

                contentLength += write("--$boundary")
                contentLength += writeln()
                contentLength += write("Content-Disposition: form-data; name=\"$fieldName\"; filename=\"$name\"")
                contentLength += writeln()
                contentLength += write("Content-Type: " + request.mediaTypes.getOrElse(i) { guessContentType(name) })
                contentLength += writeln()
                contentLength += writeln()

                // input file data
                if (output) {
                    inputStream().use {
                        it.copyTo(this, BUFFER_SIZE, progress = { writtenBytes ->
                            progressCallback?.invoke(contentLength + writtenBytes, contentLength + filesLength)
                        })
                    }
                }
                contentLength += length
                contentLength += writeln()
            }

            contentLength += write("--$boundary--")
            contentLength += writeln()
        }

        progressCallback?.invoke(contentLength, contentLength)
        if (output) {
            return InputStreamReader(PipedInputStream(stream))
        }

        return contentLength
    }

    private val boundary = retrieveBoundaryInfo(request)

    init {
        // TODO: actually change how dataparts / blobs are injected.
        request.body = Body(
            { stream(true) as Reader },
            stream(false) as Long
        )
    }
}

fun OutputStream?.write(str: String): Int {
    val data = str.toByteArray()
    this?.write(data)
    return data.size
}

fun OutputStream?.writeln(): Int {
    this?.write(CRLF)
    return CRLF.size
}

private const val BUFFER_SIZE = 1024
private val CRLF = "\r\n".toByteArray()

private fun guessContentType(name: String): String = try {
    URLConnection.guessContentTypeFromName(name) ?: "application/octet-stream"
} catch (ex: NoClassDefFoundError) {
    // The MimetypesFileTypeMap class doesn't exists on old Android devices.
    "application/octet-stream"
}

fun retrieveBoundaryInfo(request: Request): String {
    return request[Headers.CONTENT_TYPE].lastOrNull()?.split("boundary=", limit = 2)?.getOrNull(1)
            ?: System.currentTimeMillis().toString(16)
}
