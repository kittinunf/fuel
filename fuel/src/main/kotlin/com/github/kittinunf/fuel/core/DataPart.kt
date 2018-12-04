package com.github.kittinunf.fuel.core

import java.io.File
import java.io.InputStream
import java.net.URLConnection

typealias LazyDataPart = (request: Request) -> DataPart

// https://tools.ietf.org/html/rfc7578
//
// 4.2.  Content-Disposition Header Field for Each Part
//
//   Each part MUST contain a Content-Disposition header field [RFC2183]
//   where the disposition type is "form-data".  The Content-Disposition
//   header field MUST also contain an additional parameter of "name"; the
//   value of the "name" parameter is the original field name from the
//   form (possibly encoded; see Section 5.1).  For example, a part might
//   contain a header field such as the following, with the body of the
//   part containing the form data of the "user" field:
//
//      Content-Disposition: form-data; name="user"
//
// 4.4.  Content-Type Header Field for Each Part
//
//   Each part MAY have an (optional) "Content-Type" header field, which
//   defaults to "text/plain".  If the contents of a file are to be sent,
//   the file data SHOULD be labeled with an appropriate media type, if
//   known, or "application/octet-stream".
//
sealed class DataPart {
    abstract fun inputStream(): InputStream
    abstract val contentDisposition: String
    abstract val contentType: String
    abstract val contentLength: Long?

    companion object {
        internal const val GENERIC_BYTE_CONTENT = "application/octet-stream"
        internal const val GENERIC_CONTENT = "text/plain"
    }
}

data class InlineDataPart(
    val content: String,
    val name: String,
    override val contentType: String = GENERIC_CONTENT,
    override val contentDisposition: String = "form-data; name=\"$name\""
): DataPart() {
    override fun inputStream() = content.byteInputStream()
    override val contentLength get() = content.length.toLong()
}

// 4.3.  Multiple Files for One Form Field
//
//   The form data for a form field might include multiple files.
//
//   [RFC2388] suggested that multiple files for a single form field be
//   transmitted using a nested "multipart/mixed" part.  This usage is
//   deprecated.

data class FileDataPart(
    val file: File,
    val name: String = file.nameWithoutExtension,
    val fileName: String? = file.name,
    override val contentType: String = FileDataPart.guessContentType(file),
    // For form data that represents the content of a file, a name for the
    //   file SHOULD be supplied as well, by using a "filename" parameter of
    //   the Content-Disposition header field.  The file name isn't mandatory
    //   for cases where the file name isn't available or is meaningless or
    //   private; this might result, for example, when selection or drag-and-
    //   drop is used or when the form data content is streamed directly from
    //   a device.
    override val contentDisposition: String = "form-data; name=\"$name\"; filename=\"$fileName\""
) : DataPart() {
    override fun inputStream() = file.inputStream()
    override val contentLength get() = file.length()

    companion object {
        fun guessContentType(file: File) = try {
            URLConnection.guessContentTypeFromName(file.name) ?:
                BlobDataPart.guessContentType(file.inputStream())
        } catch (ex: NoClassDefFoundError) {
            // The MimetypesFileTypeMap class doesn't exists on old Android devices.
            GENERIC_BYTE_CONTENT
        }
    }
}

data class BlobDataPart(
    val inputStream: InputStream,
    val name: String,
    val fileName: String? = null,
    override val contentLength: Long? = null,
    override val contentType: String = BlobDataPart.guessContentType(inputStream),
    override val contentDisposition: String = "form-data; name=\"$name\"${if (fileName != null) "; filename=\"$fileName\"" else "" }"
) : DataPart() {
    override fun inputStream() = inputStream
    companion object {
        fun guessContentType(stream: InputStream) = try {
            URLConnection.guessContentTypeFromStream(stream) ?:
            GENERIC_BYTE_CONTENT
        } catch (ex: NoClassDefFoundError) {
            // The MimetypesFileTypeMap class doesn't exists on old Android devices.
            GENERIC_BYTE_CONTENT
        }
    }
}
