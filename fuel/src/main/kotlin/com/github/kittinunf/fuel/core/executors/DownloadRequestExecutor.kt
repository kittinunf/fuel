package com.github.kittinunf.fuel.core.executors

import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.requests.DownloadRequest
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
            response.dataStream.copyTo(out = it)
        }

        // This allows the stream to be written to disk first and then return the written file.
        return response.copy(dataStream = FileInputStream(file))
    }
}