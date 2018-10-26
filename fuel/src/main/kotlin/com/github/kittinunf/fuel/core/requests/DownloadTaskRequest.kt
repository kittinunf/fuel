package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.ProgressCallback
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.util.copyTo
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL

internal class DownloadTaskRequest(request: Request) : TaskRequest(request) {
    var progressCallback: ProgressCallback? = null
    lateinit var destinationCallback: ((Response, URL) -> File)

    override fun call(): Response {
        val response = super.call()
        val file = destinationCallback(response, request.url)

        FileOutputStream(file).use {
            response.dataStream.copyTo(out = it, bufferSize = BUFFER_SIZE, progress = { readBytes ->
                // The Content-Length can *ONLY* be used if it's present, which it is not if the response is chunked or
                // encoded (e.g. gzipped). We can opt to not send progress reports, send -1 or act as if it's at 100%
                // constantly.
                val totalBytes = if (response.contentLength > 0) { response.contentLength } else { readBytes }
                progressCallback?.invoke(readBytes, totalBytes)
            })
        }

        // This allows the stream to be written to disk first and then return the written file.
        return response.copy(dataStream = FileInputStream(file))
    }
}

private const val BUFFER_SIZE = 1024
