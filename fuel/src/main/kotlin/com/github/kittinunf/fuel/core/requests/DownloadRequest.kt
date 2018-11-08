package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.DefaultBody
import com.github.kittinunf.fuel.core.ProgressCallback
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL

class DownloadRequest(private val wrapped: Request) : Request by wrapped {
    override val request: DownloadRequest = this
    override fun toString() = "Download[\n\r\t$wrapped\n\r]"

    internal lateinit var destinationCallback: ((Response, URL) -> File)

    init {
        executionOptions.transformResponse(this::transformResponse)
    }

    fun destination(destination: (Response, URL) -> File): DownloadRequest {
        destinationCallback = destination
        return request
    }

    fun progress(progress: ProgressCallback) = responseProgress(progress)

    private fun transformResponse(request: Request, response: Response): Response {
        val file = this.destinationCallback(response, request.url)
        val totalBytes = FileOutputStream(file).use { outputStream ->
            response.body.toStream().use { inputStream ->
                inputStream.copyTo(out = outputStream)
            }
        }

        // This allows the stream to be written to disk first and then return the written file.
        return response.copy(body = DefaultBody.from({ FileInputStream(file) }, { totalBytes }))
    }

    companion object {
        fun enableFor(request: Request) = request.enabledFeatures
            .getOrPut(DownloadRequest::class.java.canonicalName) { DownloadRequest(request) } as DownloadRequest
    }
}

fun Request.download(): DownloadRequest = DownloadRequest.enableFor(this)
