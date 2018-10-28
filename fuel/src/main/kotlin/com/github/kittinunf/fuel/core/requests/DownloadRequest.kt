package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.executors.DownloadRequestExecutor
import java.io.File
import java.net.URL

typealias DownloadDestinationCallback = (Response, URL) -> File

data class DownloadRequest(
    override val request: Request
) : Request by request {
    lateinit var destination: DownloadDestinationCallback

    override fun destination(destination: DownloadDestinationCallback): DownloadRequest {
        this.destination = destination
        return this
    }

    companion object {
        fun from(request: Request): DownloadRequest {
            // Pass-through if request is already download
            return when (request) {
                is DownloadRequest -> request
                else -> DownloadRequest(request).apply {
                    executor = DownloadRequestExecutor(executor, this)
                }
            }
        }
    }
}
