package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.DefaultBody
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL

internal class DownloadTaskRequest(request: Request) : TaskRequest(request) {
    lateinit var destinationCallback: ((Response, URL) -> File)

    override fun call(): Response {
        val response = super.call()
        val file = destinationCallback(response, request.url)
        val totalBytes = FileOutputStream(file).use { outputStream ->
            response.body.toStream().use { inputStream ->
                inputStream.copyTo(out = outputStream)
            }
        }

        // This allows the stream to be written to disk first and then return the written file.
        return response.copy(body = DefaultBody.from({ FileInputStream(file) }, { totalBytes }))
    }
}
