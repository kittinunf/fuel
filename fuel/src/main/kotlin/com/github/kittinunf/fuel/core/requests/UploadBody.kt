package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.Body
import com.github.kittinunf.fuel.core.DataPart
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.representationOfBytes
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

internal class BoundaryMissing(request: UploadRequest) : FuelError(
    IllegalArgumentException(
        "The Request is missing the boundary parameter in its Content-Type.\n\n" +
        "This can happen if you manually overwrite the Content-Type but forget to set a boundary. The boundary is \n" +
        "normally set automatically when you call \"request.upload()\". Remove manually setting the Content-Type or \n" +
        "add the boundary parameter to the Content-Type for this request: \n\n" +
            "\trequest.header(Headers.ContentType, \"multipart/form-data; boundary=custom-boundary\")"
    ),
    Response.error(request.url)
)

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
        val lazyDataparts = request.dataParts

        return outputStream.buffered().let { stream ->
            // Parameters
            val parameterLength = request.parameters.sumByDouble { (name, data) ->
                writeParameter(stream, name, data).toDouble()
            }

            // Blobs / Files
            val filesWithHeadersLength = lazyDataparts.sumByDouble { lazyDataPart ->
                writeDataPart(stream, lazyDataPart(request)).toDouble()
            }

            // Sum and Trailer
            val writtenLength = 0L +
                parameterLength +
                filesWithHeadersLength +
                stream.writeBoundary() + stream.writeString("--") +
                stream.writeNewline()

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
            request.dataParts.sumByDouble { lazyDataPart ->
                val dataPart = lazyDataPart(request)

                // Allow for unknown sizes
                val length = dataPart.contentLength ?: return@lazy null
                if (length == -1L) return@lazy -1L

                0.0 + writeDataPartHeader(ByteArrayOutputStream(), dataPart) + length + CRLF.size
            } +

            // Trailer size
            "--$boundary--".toByteArray(DEFAULT_CHARSET).size + CRLF.size
        ).toLong()
    }

    private val boundary: String by lazy {
        request[Headers.CONTENT_TYPE].lastOrNull()
            ?.let { Regex("boundary=([^\\s]+)").find(it)?.groupValues?.getOrNull(1)?.trim('"') }
            ?: throw BoundaryMissing(request)
    }

    private fun writeParameter(outputStream: OutputStream, name: String, data: Any?): Long {
        outputStream.apply {
            return 0L +
                writeBoundary() +
                writeNewline() +
                writeString("${Headers.CONTENT_DISPOSITION}: form-data; name=\"$name\"") +
                writeNewline() +
                writeString("${Headers.CONTENT_TYPE}: text/plain; charset=\"${DEFAULT_CHARSET.name()}\"") +
                writeNewline() +
                writeNewline() +
                writeString(data.toString()) +
                writeNewline()
        }
    }

    private fun writeDataPart(outputStream: OutputStream, dataPart: DataPart): Long {
        outputStream.apply {
            val headerLength = writeDataPartHeader(outputStream, dataPart)
            val dataLength = dataPart.inputStream().use { it.copyTo(this) }
            return headerLength + dataLength + writeNewline()
        }
    }

    private fun writeDataPartHeader(outputStream: OutputStream, dataPart: DataPart): Long {
        outputStream.apply {
            return 0L +
                writeBoundary() +
                writeNewline() +
                writeString("${Headers.CONTENT_DISPOSITION}: ${dataPart.contentDisposition}") +
                writeNewline() +
                writeString("${Headers.CONTENT_TYPE}: ${dataPart.contentType}") +
                writeNewline() +
                writeNewline()
        }
    }

    private fun OutputStream.writeNewline() = writeBytes(CRLF)
    private fun OutputStream.writeBytes(bytes: ByteArray) = write(bytes).let { bytes.size.toLong() }
    private fun OutputStream.writeString(string: String, charset: Charset = DEFAULT_CHARSET) = writeBytes(string.toByteArray(charset))
    private fun OutputStream.writeBoundary() = writeString("--$boundary")

    companion object {
        val DEFAULT_CHARSET = Charsets.UTF_8
        private val CRLF = "\r\n".toByteArray(DEFAULT_CHARSET)

        fun from(request: UploadRequest): Body {
            return UploadBody(request).apply {
                inputAvailable = true
            }
        }
    }
}
