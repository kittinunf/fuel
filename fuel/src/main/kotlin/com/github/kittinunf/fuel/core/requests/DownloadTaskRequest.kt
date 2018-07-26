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
            response.data.inputStream().copyTo(it, BUFFER_SIZE) { readBytes ->
                progressCallback?.invoke(readBytes, response.contentLength)
            }
        }
        return response
    }
}

private const val BUFFER_SIZE = 1024
