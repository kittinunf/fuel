package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.DataPart
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.MultipartBody
import com.github.kittinunf.fuel.core.Request
import java.io.File
import java.net.URL
import java.util.Collections

typealias DataPartSource = (request: Request) -> DataPart
typealias LegacySource = (Request, URL) -> File

data class MultipartRequest(
    override val request: Request,
    private var sources: MutableCollection<DataPartSource> = mutableListOf()
) : Request by request {

    @Suppress("RedundantLambdaArrow")
    fun dataPart(vararg parts: DataPart) = parts.fold(this) { _, it -> dataPart({ _ -> it }) }
    fun dataPart(vararg parts: DataPartSource): MultipartRequest {
        sources.addAll(parts)
        return this
    }

    @JvmOverloads
    fun source(fileName: String? = null, contentType: String? = null, source: LegacySource) = dataPart({
        request -> DataPart.from(source.invoke(request, request.url), fileName = fileName, contentType = contentType)
    })

    val dataParts: Iterable<DataPart> by lazy {
        sources = Collections.unmodifiableCollection(sources)
        sources.map { it.invoke(request) }
    }

    companion object {
        fun from(request: Request): MultipartRequest {
            // Pass-through if request is already multipart, or body is already multipart
            return when (request) {
                is MultipartRequest -> request
                else -> MultipartRequest(request).apply {

                    // Currently only supports form-data
                    val boundary = System.currentTimeMillis().toString(16)
                    this[Headers.CONTENT_TYPE] = "multipart/form-data; boundary=$boundary"

                    body = when (body) {
                        is MultipartBody -> body
                        else -> MultipartBody.from(this)
                    }
                }
            }
        }
    }
}
