package com.github.kittinunf.fuel.core.executors

import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.requests.DownloadRequest
import com.github.kittinunf.fuel.util.copyTo
import java.io.FileInputStream
import java.io.FileOutputStream

data class DownloadRequestExecutor(
    val wrapped: RequestExecutor,
    val originalDownloadRequest: DownloadRequest
) : RequestExecutor by wrapped {

    override fun call(): Response {
        val response = wrapped.call() as Response
        val file = originalDownloadRequest.destination.invoke(response, request.url)

        FileOutputStream(file).use {
            response.dataStream.copyTo(out = it, onProgress = { readBytes ->
                // The Content-Length can *ONLY* be used if it's present, which it is not if the response is chunked or
                // encoded (e.g. gzipped). We can opt to not send progress reports, send -1 or act as if it's at 100%
                // constantly.
                val totalBytes = if (response.contentLength > 0) {
                    response.contentLength
                } else {
                    readBytes
                }
                request.progress.invoke(readBytes, totalBytes)
            })
        }

        // This allows the stream to be written to disk first and then return the written file.
        return response.copy(dataStream = FileInputStream(file))
    }
}