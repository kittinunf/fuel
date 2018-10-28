package com.github.kittinunf.fuel.core

import java.io.File
import java.io.FileInputStream
import java.nio.charset.Charset

/**
 * https://tools.ietf.org/html/rfc2183
 */
enum class ContentDispositionType(val value: String) {
    INLINE("inline"),
    ATTACHMENT("attachment"),
    FORM_DATA("form-data")
}

data class DataPart(
    val partName: String,
    val fileName: String?,
    val contentType: String?,
    val type: ContentDispositionType,
    val dataPartBody: Body
) : Body by dataPartBody {
    companion object {
        val DEFAULT_CONTENT_TYPE = "application/octet-stream"

        fun from(
            file: File,
            length: Number? = null,
            partName: String? = null,
            fileName: String? = null,
            contentType: String? = null,
            charset: Charset = Charsets.UTF_8
        ): DataPart {

            val calculateLength = when (charset == Charsets.UTF_8) {
                true -> length?.let { { it } } ?: { file.length() }
                false -> null
            }

            return from(
                partName = partName ?: file.nameWithoutExtension,
                fileName = fileName ?: file.name,
                contentType = contentType,
                dataPartBody = DefaultBody.from(
                    openStream = { FileInputStream(file) },
                    calculateLength = calculateLength,
                    charset = charset
                )
            )
        }

        fun from(partName: String, fileName: String? = null, contentType: String? = null, dataPartBody: Body) = DataPart(
            partName = partName,
            fileName = fileName,
            contentType = contentType,
            type = ContentDispositionType.FORM_DATA,
            dataPartBody = dataPartBody
        )

        fun from(blob: Blob, fileName: String? = null, contentType: String? = null, charset: Charset = Charsets.UTF_8) = DataPart(
            partName = blob.name,
            fileName = fileName,
            contentType = contentType,
            type = ContentDispositionType.FORM_DATA,
            dataPartBody = DefaultBody.from(
                openStream = { blob.inputStream.invoke() },
                calculateLength = { blob.length },
                charset = charset
            )
        )
    }
}
