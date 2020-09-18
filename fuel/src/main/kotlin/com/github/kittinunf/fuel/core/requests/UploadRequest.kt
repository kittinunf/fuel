package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.Blob
import com.github.kittinunf.fuel.core.DataPart
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.LazyDataPart
import com.github.kittinunf.fuel.core.ProgressCallback
import com.github.kittinunf.fuel.core.Request
import java.io.File
import java.net.URL
import java.util.UUID

class UploadRequest private constructor(private val wrapped: Request) : Request by wrapped {
    override val request: UploadRequest = this
    val dataParts: MutableCollection<LazyDataPart> = mutableListOf()

    private fun ensureBoundary() {
        val contentType = this[Headers.CONTENT_TYPE].lastOrNull()

        // Overwrite the current content type
        if (contentType.isNullOrBlank() || !contentType.startsWith("multipart/form-data") || !Regex("boundary=[^\\s]+").containsMatchIn(contentType)) {
            this[Headers.CONTENT_TYPE] = "multipart/form-data; boundary=\"${UUID.randomUUID()}\""
            return
        }
    }

    override fun toString() = "Upload[\n\r\t$wrapped\n\r]"

    /**
     * Add one or multiple [dataParts] callbacks to be invoked to get a [DataPart] to write to the [UploadBody]
     * @param dataParts [LazyDataPart] the callbacks
     */
    fun add(vararg dataParts: LazyDataPart) = dataParts.fold(this, UploadRequest::plus)

    /**
     * Add one or multiple [DataPart]s to the [UploadBody]
     * @param dataParts [DataPart] the parts
     */
    fun add(vararg dataParts: DataPart) = plus(dataParts.toList())

    /**
     * Add a [DataPart] callback to be invoked to get a [DataPart] to write to the [UploadBody]
     * @param dataPart [LazyDataPart] the callback
     */
    fun add(dataPart: LazyDataPart) = plus(dataPart)

    /**
     * Add a [DataPart] to be written to the [UploadBody]
     * @param dataPart [DataPart] the part
     */
    fun add(dataPart: DataPart) = plus(dataPart)

    /**
     * Add a [DataPart] callback to be invoked to get a [DataPart] to write to the [UploadBody]
     * @param dataPart [LazyDataPart] the callback
     */
    operator fun plus(dataPart: LazyDataPart) = this.also { dataParts.add(dataPart) }

    /**
     * Add a [DataPart] to be written to the [UploadBody]
     * @param dataPart [DataPart] the part
     */
    operator fun plus(dataPart: DataPart) = plus { dataPart }

    /**
     * Add all [dataParts] to be written to the [UploadBody]
     * @param dataParts [Iterable<DataPart>] the iterable that yields [DataPart]s
     */
    operator fun plus(dataParts: Iterable<DataPart>) = dataParts.fold(this) { acc, dataPart ->
        acc.plus(dataPart)
    }

    /**
     * Add a [ProgressCallback] to the [requestProgress]
     * @param progress [ProgressCallback] the callback
     */
    fun progress(progress: ProgressCallback) = requestProgress(progress)

    companion object {
        val FEATURE: String = UploadRequest::class.java.canonicalName

        /**
         * Enable [UploadRequest] for the passed in [request]
         *
         * @note this sets the Content-Type to multipart/form-data; boundary=uuid even when there was already a
         *   Content-Type set, unless it's multipart/form-data with a valid boundary.
         *
         * @param request [Request] the request to enable this for
         * @return [UploadRequest] the enhanced request
         */
        fun enableFor(request: Request) = request.enabledFeatures
            .getOrPut(FEATURE) {
                UploadRequest(request)
                    .apply { this.body(UploadBody.from(this)) }
                    .apply { this.ensureBoundary() }
            } as UploadRequest
    }

    @Deprecated("Use request.add({ BlobDataPart(...) }, { ... }, ...) instead", ReplaceWith(""), DeprecationLevel.ERROR)
    fun blobs(@Suppress("DEPRECATION") blobsCallback: (Request, URL) -> Iterable<Blob>): UploadRequest =
        throw NotImplementedError("request.blobs has been removed. Use request.add({ BlobDataPart(...) }, { ... }, ...) instead.")

    @Deprecated("Use request.add { BlobDataPart(...) } instead", ReplaceWith("add(blobsCallback)"))
    fun blob(@Suppress("DEPRECATION") blobCallback: (Request, URL) -> Blob): UploadRequest =
        throw NotImplementedError("request.blob has been removed. Use request.add { BlobDataPart(...) } instead.")

    @Deprecated("Use request.add({ ... }, { ... }, ...) instead", ReplaceWith(""), DeprecationLevel.ERROR)
    fun dataParts(dataPartsCallback: (Request, URL) -> Iterable<DataPart>): UploadRequest =
        throw NotImplementedError("request.dataParts has been removed. Use request.add { XXXDataPart(...) } instead.")

    @Deprecated("Use request.add({ FileDataPart(...) }, { ... }, ...) instead", ReplaceWith(""), DeprecationLevel.ERROR)
    fun sources(sourcesCallback: (Request, URL) -> Iterable<File>): UploadRequest =
        throw NotImplementedError("request.sources has been removed. Use request.add({ BlobDataPart(...) }, { ... }, ...) instead.")

    @Deprecated("Use request.add { FileDataPart(...)} instead", ReplaceWith("add(sourceCallback)"), DeprecationLevel.ERROR)
    fun source(sourceCallback: (Request, URL) -> File): UploadRequest =
        throw NotImplementedError("request.source has been removed. Use request.add { FileDataPart(...) } instead.")

    @Deprecated("Set the name via DataPart (FileDataPart, InlineDataPart, BlobDataPart) instead", ReplaceWith(""), DeprecationLevel.ERROR)
    fun name(nameCallback: () -> String): UploadRequest =
        throw NotImplementedError("request.name has been removed. Set the name via DataPart (FileDataPart, InlineDataPart, BlobDataPart) instead")

    @Deprecated("Set the name via DataPart (FileDataPart, InlineDataPart, BlobDataPart) instead", ReplaceWith(""), DeprecationLevel.ERROR)
    fun name(newName: String): UploadRequest =
        throw NotImplementedError("request.name has been removed. Set the name via DataPart (FileDataPart, InlineDataPart, BlobDataPart) instead")
}
fun Request.upload(): UploadRequest = UploadRequest.enableFor(this)
