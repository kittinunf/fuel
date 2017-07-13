package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.util.copyTo
import com.github.kittinunf.fuel.util.toHexString
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.net.URL
import java.net.URLConnection

class UploadTaskRequest(request: Request) : TaskRequest(request) {

    var bodyCallBack = fun (request: Request, outputStream: OutputStream?, totalLength: Long): Long {
        fun write(str: String): Int {
            val data = str.toByteArray()
            outputStream?.write(data)
            return data.size
        }

        var contentLength = 0L
        outputStream.apply {
            val files = sourceCallback.invoke(request, request.url)

            files.forEachIndexed { i, file ->
                val postFix = if (files.count() == 1) "" else "${i + 1}"
                val fileName = request.names.getOrElse(i) { request.name + postFix }

                contentLength += write("--$boundary")
                contentLength += write(CRLF)
                contentLength += write("Content-Disposition: form-data; name=\"$fileName\"; filename=\"${file.name}\"")
                contentLength += write(CRLF)
                contentLength += write("Content-Type: " + request.mediaTypes.getOrElse(i) { guessContentType(file) })
                contentLength += write(CRLF)
                contentLength += write(CRLF)

                //input file data
                if (outputStream != null) {
                    FileInputStream(file).use {
                        it.copyTo(outputStream, BUFFER_SIZE) { writtenBytes ->
                            progressCallback?.invoke(contentLength + writtenBytes, totalLength)
                        }
                    }
                }
                contentLength += file.length()
                contentLength += write(CRLF)
            }

            request.parameters.forEach {
                contentLength += write("--$boundary")
                contentLength += write(CRLF)
                contentLength += write("Content-Disposition: form-data; name=\"${it.first}\"")
                contentLength += write(CRLF)
                contentLength += write("Content-Type: text/plain")
                contentLength += write(CRLF)
                contentLength += write(CRLF)
                contentLength += write(it.second.toString())
                contentLength += write(CRLF)
            }

            contentLength += write("--$boundary--")
            contentLength += write(CRLF)
        }

        progressCallback?.invoke(contentLength, totalLength)
        return contentLength
    }

    val BUFFER_SIZE = 1024

    val CRLF = "\r\n"
    val boundary = request.httpHeaders["Content-Type"]?.split("=", limit = 2)?.get(1) ?: System.currentTimeMillis().toHexString()

    var progressCallback: ((Long, Long) -> Unit)? = null
    lateinit var sourceCallback: (Request, URL) -> Iterable<File>

    init{
        request.bodyCallback = bodyCallBack
    }

    private fun guessContentType(file: File): String {
        try {
            return URLConnection.guessContentTypeFromName(file.name) ?: "application/octet-stream"
        } catch (ex: NoClassDefFoundError) {
            // The MimetypesFileTypeMap class doesn't exists on old Android devices.
            return "application/octet-stream"
        }
    }
}

