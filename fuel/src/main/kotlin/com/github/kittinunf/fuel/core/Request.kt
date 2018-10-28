package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.deserializers.ByteArrayDeserializer
import com.github.kittinunf.fuel.core.deserializers.StringDeserializer
import com.github.kittinunf.fuel.core.requests.DownloadTaskRequest
import com.github.kittinunf.fuel.core.requests.TaskRequest
import com.github.kittinunf.fuel.core.requests.UploadSourceCallback
import com.github.kittinunf.fuel.core.requests.UploadTaskRequest
import com.github.kittinunf.fuel.util.encodeBase64ToString
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.charset.Charset

typealias Parameters = List<Pair<String, Any?>>

interface Request : RequestOptions, RequestExecutions, RequestRepresentation, RequestAuthentication, Fuel.RequestConvertible {
    val method: Method
    val url: URL
    val parameters: Parameters
    var executor: RequestExecutor

    val headers: Headers
    operator fun get(header: String): HeaderValues
    operator fun set(header: String, values: Collection<*>): Request
    operator fun set(header: String, value: Any): Request

    fun header(header: String): HeaderValues
    fun header(map: Map<String, Any>): Request
    fun header(vararg pairs: Pair<String, Any>): Request
    fun header(header: String, values: Collection<*>): Request
    fun header(header: String, value: Any): Request
    fun header(header: String, vararg values: Any): Request
    fun appendHeader(header: String, value: Any): Request
    fun appendHeader(header: String, vararg values: Any): Request
    fun appendHeader(vararg pairs: Pair<String, Any>): Request

    var body: Body
    fun body(openStream: BodySource, calculateLength: BodyLength? = null, charset: Charset = Charsets.UTF_8): Request
    fun body(stream: InputStream, calculateLength: BodyLength? = null, charset: Charset = Charsets.UTF_8): Request
    fun body(bytes: ByteArray, charset: Charset = Charsets.UTF_8): Request
    fun body(body: String, charset: Charset = Charsets.UTF_8): Request
    fun body(file: File, charset: Charset = Charsets.UTF_8): Request
    fun jsonBody(body: String, charset: Charset = Charsets.UTF_8): Request

    val progress: Progress
    fun progress(handler: ProgressCallback): Request
    fun progress(handlers: Progress): Request

    fun multipart(): MultipartRequest
    fun download(): DownloadRequest
    fun destination(destination: DownloadDestinationCallback): DownloadRequest
}

interface RequestExecutions {
    fun response(handler: HandlerWithResult<ByteArray>): CancellableRequest
    fun response(handler: Handler<ByteArray>): CancellableRequest
    fun response(): Triple<Request, Response, Result<ByteArray, FuelError>>

    fun responseString(charset: Charset = Charsets.UTF_8, handler: HandlerWithResult<String>): CancellableRequest
    fun responseString(charset: Charset, handler: Handler<String>): CancellableRequest
    fun responseString(handler: Handler<String>): CancellableRequest

    fun responseString(charset: Charset = Charsets.UTF_8): Triple<Request, Response, Result<String, FuelError>>

    fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>, handler: HandlerWithResult<T>): CancellableRequest
    fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>, handler: Handler<T>): CancellableRequest
    fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>): Triple<Request, Response, Result<T, FuelError>>
}

interface RequestRepresentation {
    override fun toString(): String
    fun httpString(): String
    fun cUrlString(): String
}

interface RequestAuthentication {
    fun authenticate(username: String, password: String): Request
    fun basicAuthentication(username: String, password: String): Request
    fun bearerAuthentication(token: String): Request
}