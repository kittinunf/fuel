package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Manager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.util.copyTo
import java.io.*
import java.net.URL

class DownloadTaskRequest(request: Request) : TaskRequest(request) {
    val BUFFER_SIZE = 1024

    var progressCallback: ((Long, Long) -> Unit)? = null
    lateinit var destinationCallback: ((Response, URL) -> File)

    lateinit var dataStream: InputStream
    lateinit var fileOutputStream: FileOutputStream

    override fun call(): Response {
        try {
            val response = Manager.instance.client.executeRequest(request)
            val file = destinationCallback.invoke(response, request.url)
            //file output
            fileOutputStream = FileOutputStream(file)
            dataStream = ByteArrayInputStream(response.data)
            dataStream.copyTo(fileOutputStream, BUFFER_SIZE) { readBytes ->
                progressCallback?.invoke(readBytes, response.httpContentLength)
            }
            return response.apply { dispatchCallback(this) }
        } catch(error: FuelError) {
            if (error.exception as? InterruptedIOException != null) {
                interruptCallback?.invoke(request)
            }
            throw error
        }
    }
}