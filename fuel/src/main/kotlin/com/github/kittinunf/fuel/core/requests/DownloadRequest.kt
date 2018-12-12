package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.ProgressCallback
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URL

typealias LegacyDestinationCallback = (Response, URL) -> File
typealias FileDestinationCallback = (Response, Request) -> File
typealias StreamDestinationCallback = (Response, Request) -> Pair<OutputStream, DestinationAsStreamCallback>
typealias DestinationAsStreamCallback = () -> InputStream

class DownloadRequest private constructor(private val wrapped: Request) : Request by wrapped {
    override val request: DownloadRequest = this
    override fun toString() = "Download[\n\r\t$wrapped\n\r]"

    private lateinit var destinationCallback: StreamDestinationCallback

    init {
        executionOptions += this::transformResponse
    }

    @Deprecated("Use fileDestination with (Request, Response) -> File")
    fun destination(destination: LegacyDestinationCallback) =
        fileDestination { response: Response, request: Request -> destination(response, request.url) }

    /**
     * Set the destination callback
     *
     * @note destination *MUST* be writable or this may or may not silently fail, dependent on the JVM/engine this is
     *   called on. For example, Android silently fails and hangs if used an inaccessible temporary directory, which
     *   is syntactically valid.
     *
     * @param destination [FileDestinationCallback] callback called with the [Response] and [Request]
     * @return [DownloadRequest] self
     */
    fun fileDestination(destination: FileDestinationCallback) =
        streamDestination { response: Response, request: Request ->
            destination(response, request).let { file -> Pair(FileOutputStream(file), { FileInputStream(file) }) }
        }

    /**
     * Set the destination callback
     *
     * @note with the current implementation, the stream will be CLOSED after the body has been written to it.
     *
     * @param destination [StreamDestinationCallback] callback called with the [Response] and [Request]
     * @return []
     */
    fun streamDestination(destination: StreamDestinationCallback): DownloadRequest {
        destinationCallback = destination
        return request
    }

    fun progress(progress: ProgressCallback) = responseProgress(progress)

    private fun transformResponse(request: Request, response: Response): Response {
        val (output, inputCallback) = this.destinationCallback(response, request)
        output.use { outputStream ->
            response.body.toStream().use { inputStream ->
                inputStream.copyTo(out = outputStream)
            }
        }

        // This allows the stream to be written to disk first and then return the written file.
        // We can not calculate the length here because inputCallback might not return the actual output as we write it.
        return response.copy(body = DefaultBody.from(inputCallback, null))
    }

    companion object {
        val FEATURE: String = DownloadRequest::class.java.canonicalName
        fun enableFor(request: Request) = request.enabledFeatures
            .getOrPut(FEATURE) { DownloadRequest(request) } as DownloadRequest
    }
}

fun Request.download(): DownloadRequest = DownloadRequest.enableFor(this)
