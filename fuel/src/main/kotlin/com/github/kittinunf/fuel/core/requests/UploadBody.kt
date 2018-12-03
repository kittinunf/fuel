package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.Blob
import com.github.kittinunf.fuel.core.Body
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.representationOfBytes
import com.github.kittinunf.fuel.core.requests.UploadBody.Companion.DEFAULT_CHARSET
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URLConnection
import java.nio.charset.Charset

internal data class UploadBody(val request: UploadRequest) : Body {

    private var inputAvailable: Boolean = true

    /**
     * Represents this body as a string
     * @param contentType [String] the type of the content in the body, or null if a guess is necessary
     * @return [String] the body as a string or a string that represents the body such as (empty) or (consumed)
     */
    override fun asString(contentType: String?) = representationOfBytes("multipart/form-data")

    /**
     * Returns if the body is consumed.
     * @return [Boolean] if true, `writeTo`, `toStream` and `toByteArray` may throw
     */
    override fun isConsumed() = !inputAvailable

    /**
     * Returns the body emptiness.
     * @return [Boolean] if true, this body is empty
     */
    override fun isEmpty() = false

    /**
     * Returns the body as an [InputStream].
     *
     * @note callers are responsible for closing the returned stream.
     * @note implementations may choose to make the [Body] `isConsumed` and can not be written or read from again.
     *
     * @return the body as input stream
     */
    override fun toStream(): InputStream {
        throw UnsupportedOperationException(
            "Conversion `toStream` is not supported on UploadBody, because the source is not a single single stream." +
                    "Use `toByteArray` to write the contents to memory or `writeTo` to write the contents to a stream."
        )
    }

    /**
     * Returns the body as a [ByteArray].
     *
     * @note Because the body needs to be read into memory anyway, implementations may choose to make the [Body]
     *  readable once more after calling this method, with the original [InputStream] being closed (and release its
     *  resources). This also means that if an implementation choose to keep it around, `isConsumed` returns false.
     *
     * @return the entire body
     */
    override fun toByteArray(): ByteArray {
        return ByteArrayOutputStream(length?.toInt() ?: 32)
            .use { stream ->
                writeTo(stream)
                stream.toByteArray()
            }
            .also { result ->
                // The entire body is now in memory, and can act as a regular body
                request.body(DefaultBody.from(
                        { ByteArrayInputStream(result) },
                        { result.size.toLong() }
                ))
            }
    }

    /**
     * Writes the body to the [OutputStream].
     *
     * @note callers are responses for closing the [OutputStream].
     * @note implementations may choose to make the [Body] `isConsumed` and can not be written or read from again.
     * @note implementations are recommended to buffer the output stream if they can't ensure bulk writing.
     *
     * @param outputStream [OutputStream] the stream to write to
     * @return [Long] the number of bytes written
     */
    override fun writeTo(outputStream: OutputStream): Long {
        if (!inputAvailable) {
            throw FuelError.wrap(IllegalStateException(
                    "The inputs have already been written to an output stream and can not be consumed again."
            ))
        }

        inputAvailable = false
        val sourceCallback = request.sourceCallback

        return outputStream.buffered().let { stream ->
            // Parameters
            val parameterLength = request.parameters.sumByDouble { (name, data) ->
                writeParameter(stream, name, data).toDouble()
            }

            // Blobs / Files
            val files = sourceCallback(request, request.url)
            val filesWithHeadersLength = files.withIndex().sumByDouble { (i, blob) ->
                writeBlob(stream, i, blob).toDouble()
            }

            // Sum and Trailer
            val writtenLength = 0L +
                    parameterLength +
                    filesWithHeadersLength +
                    stream.writeString("--$boundary--") +
                    stream.writeBytes(CRLF)

            // This is a buffered stream, so flush what's remaining
            writtenLength.toLong().also { stream.flush() }
        }
    }

    /**
     * Returns the length of the body in bytes
     * @return [Long?] the length in bytes, null if it is unknown
     */
    override val length: Long? by lazy {
        (
            // Parameters size
            request.parameters.sumByDouble { (name, data) ->
                writeParameter(ByteArrayOutputStream(), name, data).toDouble()
            } +

                // Blobs / Files size
                request.sourceCallback(request, request.url).withIndex().sumByDouble { (index, blob) ->
                    0.0 + writeBlobHeader(ByteArrayOutputStream(), index, blob) + blob.length + CRLF.size
                } +

                // Trailer size
                "--$boundary--".toByteArray(DEFAULT_CHARSET).size + CRLF.size
            ).toLong()
    }

    private val boundary: String by lazy { retrieveBoundaryInfo(request) }

    private fun writeParameter(outputStream: OutputStream, name: String, data: Any?): Long {
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

    private fun writeBlob(outputStream: OutputStream, index: Int, blob: Blob): Long {
        outputStream.apply {
            val headerLength = writeBlobHeader(outputStream, index, blob)
            blob.inputStream().use { it.copyTo(this) }
            return headerLength + blob.length + writeBytes(CRLF)
        }
    }

    private fun writeBlobHeader(outputStream: OutputStream, index: Int, blob: Blob): Long {
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

        fun from(request: UploadRequest): Body {
            return UploadBody(request).apply {
                inputAvailable = true
            }
        }
    }
}

private fun OutputStream.writeString(string: String, charset: Charset = DEFAULT_CHARSET): Long {
    val bytes = string.toByteArray(charset)
    write(bytes)
    return bytes.size.toLong()
}

private fun OutputStream.writeBytes(bytes: ByteArray): Long {
    write(bytes)
    return bytes.size.toLong()
}