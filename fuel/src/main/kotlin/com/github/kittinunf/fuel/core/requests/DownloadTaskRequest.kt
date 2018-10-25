package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.util.copyTo
import java.io.File
import java.io.FileOutputStream
import java.net.URL

internal class DownloadTaskRequest(request: Request) : TaskRequest(request) {
    var progressCallback: ((Long, Long) -> Unit)? = null
    lateinit var destinationCallback: ((Response, URL) -> File)

    override fun call(): Response {
        val response = super.call()
        val file = destinationCallback(response, request.url)

        FileOutputStream(file).use {
            response.dataStream.copyTo(out = it, bufferSize = BUFFER_SIZE, progress = { readBytes ->
                progressCallback?.invoke(readBytes, response.contentLength)
            }) {
                response.data = it
            }
        }
        return response
    }
}

private const val BUFFER_SIZE = 1024
