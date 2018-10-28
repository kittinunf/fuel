package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.core.requests.MultipartRequest
import com.github.kittinunf.fuel.util.CopyProgress
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.net.URLConnection
import java.nio.charset.Charset

data class MultipartBody(private val request: MultipartRequest) : Body {
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
                { ByteArrayInputStream(this) },
                { this.size }
            )
        }
    }

    override fun writeTo(outputStream: OutputStream, charset: Charset?, onProgress: CopyProgress?): Long {
        if (!inputAvailable) {
            throw IllegalStateException(
                "The inputs have already been written to an output stream and can not be consumed again."
            )
        }

        inputAvailable = false
        return outputStream.let { stream ->
            // Parameters
            val parametersLength = request.parameters.fold(0L) { result, parameter ->
                val (name, data) = parameter

                (result + writeParameter(stream, name, data)).apply {
                    onProgress?.invoke(this)
                }
            }

            // Blobs / Files
            val partsWithHeaders = request.dataParts.fold(0L) { result, dataPart ->
                (result + writeDataPart(stream, dataPart, onProgress = { partProgress, _ ->
                    onProgress?.invoke(result + partProgress)
                }))
            }

            // Sum and Trailer
            val writtenLength = 0L +
                parametersLength +
                partsWithHeaders +
                stream.writeString("--$boundary--") +
                stream.writeBytes(CRLF)

            onProgress?.invoke(writtenLength)

            // This is a buffered stream, so flush what's remaining
            stream.flush()

            writtenLength
        }
    }

    override val length: Number? by lazy {
        request.parameters.fold(0L) { result, parameter ->
            result + writeParameter(ByteArrayOutputStream(), parameter.first, parameter.second)
        } +
        request.dataParts.fold(0L) { result, part ->
            // If a file has no length, the entire length is unknown
            val length = part.length
            result + when (length) {
                null -> return@lazy null
                else -> writeDataPartHeader(ByteArrayOutputStream(), part) +
                        length.toLong() +
                        CRLF.size
            }
        } +
        "--$boundary--".toByteArray(DEFAULT_CHARSET).size + CRLF.size
    }

    val boundary: String by lazy { retrieveBoundaryInfo(request) }

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

    private fun writeDataPart(outputStream: OutputStream, dataPart: DataPart, onProgress: ProgressCallback): Long {
        outputStream.apply {
            val headerLength = writeDataPartHeader(outputStream, dataPart)
            val partLength = when (dataPart.length) {
                null -> null
                else -> headerLength + dataPart.length.toLong()
            }

            val totalLength = dataPart.writeTo(
                this, onProgress = { writtenBytes ->
                    val currentTotal = headerLength + writtenBytes
                    onProgress.invoke(currentTotal, partLength ?: currentTotal)
                }
            )

            return headerLength + totalLength + writeBytes(CRLF)
        }
    }

    private fun writeDataPartHeader(outputStream: OutputStream, dataPart: DataPart): Long {
        val type = dataPart.type.value
        val mediaType = dataPart.contentType ?: guessContentType(dataPart.fileName)
        val field = when (dataPart.fileName) {
            null -> ""
            else -> " ;filename=\"${dataPart.fileName}\""
        }

        outputStream.apply {
            return 0L +
                writeString("--$boundary") +
                writeBytes(CRLF) +
                writeString("${Headers.CONTENT_DISPOSITION}: $type; name=\"${dataPart.partName}\";$field") +
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

        fun guessContentType(name: String?): String = try {
            if (name == null) {
                GENERIC_BYTE_CONTENT
            } else {
                URLConnection.guessContentTypeFromName(name) ?: GENERIC_BYTE_CONTENT
            }
        } catch (ex: NoClassDefFoundError) {
            // The MimetypesFileTypeMap class doesn't exists on old Android devices.
            GENERIC_BYTE_CONTENT
        }

        fun from(request: MultipartRequest): Body {
            return MultipartBody(request = request).apply {
                inputAvailable = true
            }
        }
    }
}

private fun OutputStream.writeString(string: String, charset: Charset = MultipartBody.DEFAULT_CHARSET): Int {
    val bytes = string.toByteArray(charset)
    write(bytes)
    return bytes.size
}

private fun OutputStream.writeBytes(bytes: ByteArray): Int {
    write(bytes)
    return bytes.size
}
