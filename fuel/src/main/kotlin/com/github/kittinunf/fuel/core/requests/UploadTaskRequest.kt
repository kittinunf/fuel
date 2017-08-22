package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.Blob
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.util.copyTo
import java.io.OutputStream
import java.net.URL
import java.net.URLConnection

internal class UploadTaskRequest(request: Request) : TaskRequest(request) {
    var progressCallback: ((Long, Long) -> Unit)? = null
    lateinit var sourceCallback: (Request, URL) -> Iterable<Blob>

    private var bodyCallBack = fun(request: Request, outputStream: OutputStream?, totalLength: Long): Long {
        var contentLength = 0L
        outputStream.apply {
            val files = sourceCallback(request, request.url)

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

                //input file data
                if (outputStream != null) {
                    inputStream().use {
                        it.copyTo(outputStream, BUFFER_SIZE) { writtenBytes ->
                            progressCallback?.invoke(contentLength + writtenBytes, totalLength)
                        }
                    }
                }
                contentLength += length
                contentLength += writeln()
            }

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

            contentLength += write("--$boundary--")
            contentLength += writeln()
        }

        progressCallback?.invoke(contentLength, totalLength)
        return contentLength
    }

    private val boundary = request.httpHeaders["Content-Type"]?.split("=", limit = 2)?.get(1) ?: System.currentTimeMillis().toString(16)

    init {
        request.bodyCallback = bodyCallBack
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
