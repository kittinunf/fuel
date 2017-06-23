package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.util.copyTo
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL

class DownloadTaskRequest(request: Request) : TaskRequest(request) {

    val BUFFER_SIZE = 1024

    var progressCallback: ((Long, Long) -> Unit)? = null
    lateinit var destinationCallback: ((Response, URL) -> File)

    lateinit var fileOutputStream: FileOutputStream

    override fun call(): Response {
        val response = super.call()
        val file = destinationCallback.invoke(response, request.url)
        //file output
        fileOutputStream = FileOutputStream(file)
        response.dataStream.copyTo(fileOutputStream, BUFFER_SIZE) { readBytes ->
                progressCallback?.invoke(readBytes, response.httpContentLength)
        }
        return response
    }
}