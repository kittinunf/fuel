package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.Blob
import com.github.kittinunf.fuel.core.DataPart
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.ProgressCallback
import com.github.kittinunf.fuel.core.Request
import java.io.File
import java.net.URL

typealias UploadSourceCallback = (Request, URL) -> Iterable<Blob>

class UploadRequest private constructor(private val wrapped: Request) : Request by wrapped {
    override val request: UploadRequest = this
    lateinit var sourceCallback: UploadSourceCallback

    internal var name: String = "file"
    internal val names: MutableList<String> = mutableListOf()
    internal val mediaTypes: MutableList<String> = mutableListOf()

    override fun toString() = "Upload[\n\r\t$wrapped\n\r]"

    /**
     *  Replace each pair, using the key as header name and value as header content
     */
    fun blobs(blobsCallback: UploadSourceCallback): UploadRequest {
        sourceCallback = blobsCallback
        return request
    }

    fun blob(blobCallback: (Request, URL) -> Blob) = blobs { request, _ -> listOf(blobCallback(request, request.url)) }
    fun dataParts(dataPartsCallback: (Request, URL) -> Iterable<DataPart>): UploadRequest {
        val parts = dataPartsCallback(request, request.url)

        mediaTypes.apply {
            clear()
            addAll(parts.map { it.type })
        }

        names.apply {
            clear()
            addAll(parts.map { it.name })
        }

        sourceCallback = { _, _ ->
            parts.map { (file) -> Blob(file.name, file.length(), file::inputStream) }
        }

        return request
    }

    fun sources(sourcesCallback: (Request, URL) -> Iterable<File>): UploadRequest {
        mediaTypes.clear()
        names.clear()

        val files = sourcesCallback(request, request.url)

        sourceCallback = { _, _ ->
            files.map { Blob(it.name, it.length(), it::inputStream) }
        }

        return request
    }

    fun source(sourceCallback: (Request, URL) -> File): UploadRequest {
        sources { request, _ ->
            listOf(sourceCallback(request, request.url))
        }

        return request
    }

    fun name(nameCallback: () -> String): UploadRequest {
        return name(nameCallback())
    }

    fun name(newName: String): UploadRequest {
        name = newName
        return request
    }

    fun progress(progress: ProgressCallback) = requestProgress(progress)

    companion object {
        val FEATURE: String = UploadRequest::class.java.canonicalName
        fun enableFor(request: Request) = request.enabledFeatures
            .getOrPut(FEATURE) {
                UploadRequest(request)
                    .apply {
                        this.body(UploadBody.from(this))

                        val boundary = System.currentTimeMillis().toString(16)
                        this[Headers.CONTENT_TYPE] = "multipart/form-data; boundary=$boundary"
                    }
            } as UploadRequest
    }
}

fun Request.upload(): UploadRequest = UploadRequest.enableFor(this)
