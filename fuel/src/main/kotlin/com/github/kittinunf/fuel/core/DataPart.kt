package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.core.BlobDataPart.Companion.guessContentType
import com.github.kittinunf.fuel.core.FileDataPart.Companion.guessContentType
import java.io.File
import java.io.InputStream
import java.net.URLConnection

/**
 * Wrapper type that generates [DataPart] from a [Request]
 */
typealias LazyDataPart = (request: Request) -> DataPart

/**
 * Generic DataPart class
 *
 * @note based on https://tools.ietf.org/html/rfc7578
 *
 *  4.2.  Content-Disposition Header Field for Each Part
 *
 *      Each part MUST contain a Content-Disposition header field [RFC2183]
 *      where the disposition type is "form-data".  The Content-Disposition
 *      header field MUST also contain an additional parameter of "name"; the
 *      value of the "name" parameter is the original field name from the
 *      form (possibly encoded; see Section 5.1).  For example, a part might
 *      contain a header field such as the following, with the body of the
 *      part containing the form data of the "user" field:
 *
 *          Content-Disposition: form-data; name="user"
 *
 *   4.3.  Multiple Files for One Form Field
 *
 *      The form data for a form field might include multiple files.
 *
 *      [RFC2388] suggested that multiple files for a single form field be
 *      transmitted using a nested "multipart/mixed" part.  This usage is
 *      deprecated.
 *
 *  4.4.  Content-Type Header Field for Each Part
 *
 *      Each part MAY have an (optional) "Content-Type" header field, which
 *      defaults to "text/plain".  If the contents of a file are to be sent,
 *      the file data SHOULD be labeled with an appropriate media type, if
 *      known, or "application/octet-stream".
 */
sealed class DataPart {

    /**
     * Get the input stream that yields the contents of this [DataPart]
     * @return [InputStream] the input stream
     */
    abstract fun inputStream(): InputStream

    /**
     * The Content-Disposition header value
     */
    abstract val contentDisposition: String

    /**
     * The Content-Type header value
     */
    abstract val contentType: String

    /**
     * The content length or -1 or null if unknown
     */
    abstract val contentLength: Long?

    companion object {
        internal const val GENERIC_BYTE_CONTENT = "application/octet-stream"
        internal const val GENERIC_CONTENT = "text/plain"
    }
}

/**
 * Data part that has inline content
 *
 * @example Add metadata with a content-type
 *
 *   val metadata = "{\"description\": \"test document from Kotlin \"}
 *   request.add { InlineDataPart(metadata, contentType = "application/json", name = "metadata") }
 *
 *   // ...boundary
 *   // Content-Disposition: "form-data; name=metadata"
 *   // Content-Type: "application/json"
 *   //
 *   // {"description": "test document from Kotlin"}
 *
 * @param content [String] the inline content
 * @param name [String] the field name
 * @param filename [String] the remote file name, or null
 * @param contentType [String] the content-type of the inline content; defaults to text/plain; charset=utf-8
 * @param contentDisposition [String] defaults to form-data with name and filename set, unless filename is null
 */
data class InlineDataPart(
    val content: String,
    val name: String,
    val filename: String? = null,
    override val contentType: String = "$GENERIC_CONTENT; charset=utf-8",
    override val contentDisposition: String = "form-data; name=\"$name\"${if (filename != null) "; filename=\"$filename\"" else "" }"
) : DataPart() {
    override fun inputStream() = content.byteInputStream()
    override val contentLength get() = content.length.toLong()
}

/**
 *  DataPart that represents a file
 *
 *  @example Add a file as a DataPart
 *
 *    val file = File("foo.json")
 *    request.add { FileDataPart(file, name = "field-name", filename = "remote-file-name.json") }
 *
 *    // ...boundary
 *    // Content-Disposition: "form-data; name=metadata; filename=remote-file-name.json"
 *    // Content-Type: "application/json"
 *    //
 *    // <file-contents foo.json>
 *
 *  @example Adding multiple files under the same field name, formerly using multipart/mixed:
 *
 *    val file1 = File("foobar.json")
 *    val file2 = File("baz.json")
 *    request.add(FileDataPart(file1, name = "field-name"), FileDataPart(file2, name = "field-name"))
 *
 *    // ...boundary
 *    // Content-Disposition: "form-data; name=field-name; filename=foobar.json"
 *    // Content-Type: "application/json"
 *    //
 *    // <file-contents foobar.json>
 *    //
 *    //
 *    // ...boundary
 *    // Content-Disposition: "form-data; name=field-name; filename=baz.json"
 *    // Content-Type: "application/json"
 *    //
 *    // <file-contents baz.json>
 *
 *  @example Adding multiple files as an array to a single field name:
 *
 *    val file1 = File("foobar.json")
 *    val file2 = File("baz.json")
 *    request.add(FileDataPart(file1, name = "field-name[]"), FileDataPart(file2, name = "field-name[]"))
 *
 *    // ...boundary
 *    // Content-Disposition: "form-data; name=field-name[]; filename=foobar.json"
 *    // Content-Type: "application/json"
 *    //
 *    // <file-contents foobar.json>
 *    //
 *    //
 *    // ...boundary
 *    // Content-Disposition: "form-data; name=field-name[]; filename=baz.json"
 *    // Content-Type: "application/json"
 *    //
 *    // <file-contents baz.json>
 *
 *  @param file [File] the source file
 *  @param name [String] the field name, defaults to the file name without extension
 *  @param filename [String] the remote file name, or null
 *  @param contentType [String] the content type of the file contents; defaults to a guess using [guessContentType]
 *  @param contentDisposition [String] defaults to form-data with name and filename set, unless filename is null
 */
data class FileDataPart(
    val file: File,
    val name: String = file.nameWithoutExtension,
    val filename: String? = file.name,
    override val contentType: String = FileDataPart.guessContentType(file),
    // For form data that represents the content of a file, a name for the
    //   file SHOULD be supplied as well, by using a "filename" parameter of
    //   the Content-Disposition header field.  The file name isn't mandatory
    //   for cases where the file name isn't available or is meaningless or
    //   private; this might result, for example, when selection or drag-and-
    //   drop is used or when the form data content is streamed directly from
    //   a device.
    override val contentDisposition: String = "form-data; name=\"$name\"${if (filename != null) "; filename=\"$filename\"" else "" }"
) : DataPart() {
    override fun inputStream() = file.inputStream()
    override val contentLength get() = file.length()

    companion object {
        fun guessContentType(file: File) = try {
            URLConnection.guessContentTypeFromName(file.name)
                ?: BlobDataPart.guessContentType(file.inputStream())
        } catch (ex: NoClassDefFoundError) {
            // The MimetypesFileTypeMap class doesn't exists on old Android devices.
            GENERIC_BYTE_CONTENT
        }

        /**
         * Create a FileDataPart from a [directory] and [filename]
         *
         * @param directory [File] the directory
         * @param filename [String] the filename relative to the [directory]
         * @param name [String] the name for the field, uses [filename] without the extension by default
         * @param remoteFileName [String] the filename parameter for the DataPart, set to null to exclude
         * @param contentType [String] the Content-Type for the DataPart, set to null to [guessContentType]
         *
         * @return [FileDataPart] the DataPart
         */
        fun from(directory: String, filename: String, name: String? = null, remoteFileName: String? = filename, contentType: String? = null): DataPart =
            from(directory = File(directory), filename = filename, name = name, remoteFileName = remoteFileName, contentType = contentType)

        /**
         * Create a FileDataPart from a [directory] and [filename]
         *
         * @param directory [File] the directory
         * @param filename [String] the filename relative to the [directory]
         * @param name [String] the name for the field, uses [filename] without the extension by default
         * @param remoteFileName [String] the filename parameter for the DataPart, set to null to exclude
         * @param contentType [String] the Content-Type for the DataPart, set to null to [guessContentType]
         *
         * @return [FileDataPart] the DataPart
         */
        fun from(directory: File, filename: String, name: String? = null, remoteFileName: String? = filename, contentType: String? = null): DataPart {
            val file = File(directory, filename)
            return FileDataPart(
                file = file,
                name = name ?: file.nameWithoutExtension,
                filename = remoteFileName,
                contentType = contentType ?: guessContentType(file)
            )

        }
    }
}

/**
 * DataPart from a generic InputStream
 *
 * @note not setting the content-length or setting it to -1L, results in the default Client using chunked encoding
 *   with an arbitrary buffer size.
 *
 * @param inputStream [File] the source stream
 * @param name [String] the field name, required
 * @param filename [String] the remote file name, or null
 * @param contentLength [Long] the length in bytes, or -1 or null if it's not knows at creation time.
 * @param contentType [String] the content type of the file contents; defaults to a guess using [guessContentType]
 * @param contentDisposition [String] defaults to form-data with name and filename set, unless filename is null
 */
data class BlobDataPart(
    val inputStream: InputStream,
    val name: String,
    val filename: String? = null,
    override val contentLength: Long? = null,
    override val contentType: String = BlobDataPart.guessContentType(inputStream),
    override val contentDisposition: String = "form-data; name=\"$name\"${if (filename != null) "; filename=\"$filename\"" else "" }"
) : DataPart() {
    override fun inputStream() = inputStream
    companion object {
        fun guessContentType(stream: InputStream) = try {
            URLConnection.guessContentTypeFromStream(stream)
            ?: GENERIC_BYTE_CONTENT
        } catch (ex: NoClassDefFoundError) {
            // The MimetypesFileTypeMap class doesn't exists on old Android devices.
            GENERIC_BYTE_CONTENT
        }
    }
}
