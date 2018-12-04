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
            this[Headers.CONTENT_TYPE] = "multipart/form-data; boundary=${UUID.randomUUID()}"
            return
        }
    }

    override fun toString() = "Upload[\n\r\t$wrapped\n\r]"

    fun add(dataPart: LazyDataPart) = plus(dataPart)
    fun add(vararg dataParts: LazyDataPart) = dataParts.fold(this, UploadRequest::plus)
    fun add(dataPart: DataPart) = plus(dataPart)
    fun add(vararg dataParts: DataPart) = plus(dataParts.toList())

    operator fun plus(dataPart: LazyDataPart) = this.also { dataParts.add(dataPart) }
    operator fun plus(dataPart: DataPart) = plus { dataPart }
    operator fun plus(dataParts: Iterable<DataPart>) = dataParts.fold(this, UploadRequest::plus)

    fun progress(progress: ProgressCallback) = requestProgress(progress)

    companion object {
        val FEATURE: String = UploadRequest::class.java.canonicalName
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
