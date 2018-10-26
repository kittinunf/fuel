package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.Blob
import com.github.kittinunf.fuel.core.Body
import com.github.kittinunf.fuel.core.DefaultBody
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.ProgressCallback
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.requests.UploadBody.Companion.DEFAULT_CHARSET
import com.github.kittinunf.fuel.util.copyTo
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.URL
import java.net.URLConnection
import java.nio.charset.Charset

typealias BlobProgressCallback = (Long, Long) -> Any?
typealias UploadSourceCallback = (Request, URL) -> Iterable<Blob>

internal data class UploadBody(val request: Request, val taskRequest: UploadTaskRequest) : Body {

    private var inputAvailable: Boolean = true

    override fun isConsumed() = !inputAvailable
    override fun isEmpty() = false

    override fun toByteArray(): ByteArray {
        return ByteArrayOutputStream(length?.toInt() ?: 32).let {
            writeTo(it, null)
            it.close()
            it.toByteArray()
        }.apply {
            // The entire body is now in memory, and can act as a regular body
            request.body = DefaultBody.from(
                { InputStreamReader(ByteArrayInputStream(this)) },
                { this.size }
            )
        }
    }

    override fun writeTo(outputStream: OutputStream, charset: Charset?) {
        if (!inputAvailable) {
            throw IllegalStateException(
                "The inputs have already been written to an output stream and can not be consumed again."
            )
        }

        val sourceCallback = taskRequest.sourceCallback
        val progressCallback = taskRequest.progressCallback
        val expectedLength = length!!.toLong()

        outputStream.apply {
            // Parameters
            val parameterLength = request.parameters.sumByDouble { (name, data) ->
                writeParameter(this, name, data).toDouble()
            }

            // Blobs / Files
            var previousLastProgress = parameterLength
            val files = sourceCallback(request, request.url)
            val filesWithHeadersLength = files.withIndex().sumByDouble { (i, blob) ->
                val blobLength = writeBlob(this, i, blob, progress = { blobProgress, _ ->
                    progressCallback?.invoke(
                        (previousLastProgress + blobProgress).toLong(),
                        (expectedLength)
                    )
                })

                previousLastProgress += blobLength
                blobLength.toDouble()
            }

            // Sum and Trailer
            val writtenLength = 0L +
                parameterLength +
                filesWithHeadersLength +
                writeString("--$boundary--") +
                writeBytes(CRLF)

            progressCallback?.invoke(writtenLength.toLong(), expectedLength)

            // This is a buffered stream, so flush what's remaining
            flush()
        }

        inputAvailable = false
    }

    override val length: Number? by lazy {
        (
            // Parameters size
            request.parameters.sumByDouble { (name, data) ->
                writeParameter(ByteArrayOutputStream(), name, data).toDouble()
            } +

            // Blobs / Files size
            taskRequest.sourceCallback(request, request.url).withIndex().sumByDouble { (index, blob) ->
                0.0 + writeBlobHeader(ByteArrayOutputStream(), index, blob) + blob.length + CRLF.size
            } +

            // Trailer size
            "--$boundary--".toByteArray(DEFAULT_CHARSET).size + CRLF.size
        ).toLong()
    }

    val boundary: String by lazy { retrieveBoundaryInfo(request) }

    private fun writeParameter(outputStream: OutputStream, name: String, data: Any?) : Long {
        outputStream.apply {
            return 0L +
                writeString("--$boundary") +
                writeBytes(CRLF) +
                writeString("${Headers.CONTENT_DISPOSITION}: form-data; name=\"$name\"") +
                writeBytes(CRLF) +
                writeString("${Headers.CONTENT_TYPE}: text/plain; charset=${DEFAULT_CHARSET.name()}") +
                writeBytes(CRLF) +
                writeBytes(CRLF) +
                writeString(data.toString()) +
                writeBytes(CRLF)
        }
    }

    private fun writeBlob(outputStream: OutputStream, index: Int, blob: Blob, progress: BlobProgressCallback) : Long {
        outputStream.apply {
            val headerLength = writeBlobHeader(outputStream, index, blob)

            blob.inputStream().use {
                it.copyTo(this, PROGRESS_BUFFER_SIZE, progress = { writtenBytes ->
                    progress.invoke(headerLength + writtenBytes, headerLength + blob.length)
                })
            }

            return headerLength + blob.length + writeBytes(CRLF)
        }
    }

    private fun writeBlobHeader(outputStream: OutputStream, index: Int, blob: Blob) : Long {
        val (name, _, _) = blob
        val fieldName = request.names.getOrElse(index) { "${request.name}_${index + 1}" }
        val mediaType = request.mediaTypes.getOrElse(index) { guessContentType(name) }

        outputStream.apply {
            return 0L +
                writeString("--$boundary") +
                writeBytes(CRLF) +
                writeString("${Headers.CONTENT_DISPOSITION}: form-data; name=\"$fieldName\"; filename=\"$name\"") +
                writeBytes(CRLF) +
                writeString("${Headers.CONTENT_TYPE}: $mediaType") +
                writeBytes(CRLF) +
                writeBytes(CRLF)
        }
    }


    companion object {
        val DEFAULT_CHARSET = Charsets.UTF_8
        private const val GENERIC_BYTE_CONTENT = "application/octet-stream"
        private const val PROGRESS_BUFFER_SIZE = 1024
        private val CRLF = "\r\n".toByteArray(DEFAULT_CHARSET)

        fun retrieveBoundaryInfo(request: Request): String {
            return request[Headers.CONTENT_TYPE].lastOrNull()?.split("boundary=", limit = 2)?.getOrNull(1)
                    ?: System.currentTimeMillis().toString(16)
        }

        fun guessContentType(name: String): String = try {
            URLConnection.guessContentTypeFromName(name) ?: GENERIC_BYTE_CONTENT
        } catch (ex: NoClassDefFoundError) {
            // The MimetypesFileTypeMap class doesn't exists on old Android devices.
            GENERIC_BYTE_CONTENT
        }

        fun from(uploadTaskRequest: UploadTaskRequest, request: Request): Body {
            return UploadBody(taskRequest = uploadTaskRequest, request = request).apply {
                inputAvailable = true
            }
        }
    }

}

internal class UploadTaskRequest(request: Request) : TaskRequest(request) {
    lateinit var sourceCallback: UploadSourceCallback
    var progressCallback: ProgressCallback? = null

    init {
        request.body = UploadBody.from(this, request)
    }
}

private fun OutputStream.writeString(string: String, charset: Charset = DEFAULT_CHARSET) : Int {
    val bytes = string.toByteArray(charset)
    write(bytes)
    return bytes.size
}

private fun OutputStream.writeBytes(bytes: ByteArray) : Int {
    write(bytes)
    return bytes.size
}
