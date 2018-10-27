package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Request.Companion.toString
import com.github.kittinunf.fuel.core.deserializers.ByteArrayDeserializer
import com.github.kittinunf.fuel.core.deserializers.StringDeserializer
import com.github.kittinunf.fuel.core.requests.DownloadTaskRequest
import com.github.kittinunf.fuel.core.requests.TaskRequest
import com.github.kittinunf.fuel.core.requests.UploadTaskRequest
import com.github.kittinunf.fuel.util.encodeBase64ToString
import com.github.kittinunf.result.Result
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.net.URL
import java.nio.charset.Charset
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSocketFactory

typealias RequestTransformer = (Request) -> Request
typealias ResponseTransformer = (Request, Response) -> Response

class Request(
    val method: Method,
    val path: String,
    val url: URL,
    var type: Type = Type.REQUEST,
    val headers: Headers = Headers(),
    val parameters: List<Pair<String, Any?>> = listOf(),
    var name: String = "",
    val names: MutableList<String> = mutableListOf(),
    val mediaTypes: MutableList<String> = mutableListOf(),
    var isAllowRedirects: Boolean = true,
    var timeoutInMillisecond: Int,
    var timeoutReadInMillisecond: Int
) : Fuel.RequestConvertible {
    enum class Type {
        REQUEST,
        DOWNLOAD,
        UPLOAD
    }

    // body
    var bodyCallback: ((Request, OutputStream?, Long) -> Long)? = null

    private fun getHttpBody(): ByteArray = ByteArrayOutputStream().apply {
        bodyCallback?.invoke(request, this, 0)
    }.toByteArray()

    internal lateinit var client: Client

    // underlying task request
    internal val taskRequest: TaskRequest by lazy {
        when (type) {
            Type.DOWNLOAD -> DownloadTaskRequest(this)
            Type.UPLOAD -> UploadTaskRequest(this)
            else -> TaskRequest(this)
        }
    }

    private var taskFuture: Future<*>? = null

    // configuration
    internal var socketFactory: SSLSocketFactory? = null
    internal var hostnameVerifier: HostnameVerifier? = null

    // callers
    internal lateinit var executor: ExecutorService
    internal lateinit var callbackExecutor: Executor

    // interceptor
    internal lateinit var requestTransformer: RequestTransformer
    internal lateinit var responseTransformer: ResponseTransformer

    // interfaces
    fun timeout(timeout: Int): Request {
        timeoutInMillisecond = timeout
        return this
    }

    fun timeoutRead(timeout: Int): Request {
        timeoutReadInMillisecond = timeout
        return this
    }

    /**
     * Get the current values of the header, after normalisation of the header
     * @param header [String] the header name
     * @return the current values (or empty if none)
     */
    operator fun get(header: String): HeaderValues {
        return headers[header]
    }

    /**
     * Set the values of the header, overriding what's there, after normalisation of the header
     *
     * @param header [String] the header name
     * @param values [Collection<*>] the values to be transformed through #toString
     * @return self
     */
    operator fun set(header: String, values: Collection<*>): Request {
        headers[header] = values.map { it.toString() }
        return this
    }

    /**
     * Set the value of the header, overriding what's there, after normalisation of the header
     *
     * @param header [String] the header name
     * @param value [Any] the value to be transformed through #toString
     */
    operator fun set(header: String, value: Any): Request {
        when (value) {
            is Collection<*> -> this[header] = value
            else -> headers[header] = value.toString()
        }
        return this
    }

    /**
     * Get the current values
     *
     * @see get(header: String)
     * @return [HeaderValues] the current values
     */
    fun header(header: String) = get(header)

    /**
     * Replace the headers with the map provided
     *
     * @note In earlier versions the mapOf variant of this function worked differently than the vararg pairs variant,
     *  which has been changed to make any call to header(...) always overwrite the values and any call to
     *  appendHeader(...) will try to append the value.
     *
     * @see set(header: String, values: Collection<*>)
     * @see set(header: String, value: Any)
     *
     * @param headers [Map<String, Any>] map of headers to replace. Value can be a list or single value
     * @return [Request] the modified request
     */
    fun header(headers: Map<String, Any>) = headers.entries.fold(this) { result, entry -> result.set(entry.key, entry.value) }

    /**
     * Replace the headers with the pairs provided
     *
     * @note In earlier versions the mapOf variant of this function worked differently than the vararg pairs variant,
     *  which has been changed to make any call to header(...) always overwrite the values and any call to
     *  appendHeader(...) will try to append the value.
     *
     * @see set(header: String, values: Collection<*>)
     * @see set(header: String, value: Any)
     *
     * @param headers [Pair<String, Any>] map of headers to replace. Value can be a list or single value
     * @return [Request] the modified request
     */
    fun header(vararg headers: Pair<String, Any>) = headers.fold(this) { result, pair -> result.set(pair.first, pair.second) }

    /**
     * Replace the header with the provided values
     *
     * @see set(header: String, values: Collection<*>)
     *
     * @param header [String] the header to set
     * @param values [List<Any>] the values to set the header to
     * @return [Request] the modified request
     */
    fun header(header: String, values: Collection<*>) = set(header, values)

    /**
     * Replace the header with the provided value
     *
     * @see set(header: String, values: List<Any>)
     *
     * @param header [String] the header to set
     * @param value [Any] the value to set the header to
     * @return [Request] the modified request
     */
    fun header(header: String, value: Any): Request = set(header, value)

    /**
     * Appends the value to the header or sets it if there was none yet
     *
     * @param header [String] the header name to append to
     * @param value [Any] the value to be transformed through #toString
     */
    fun appendHeader(header: String, value: Any): Request {
        headers.append(header, value)
        return this
    }

    /**
     * Appends the value to the header or sets it if there was none yet
     *
     * @param header [String] the header name to append to
     * @param values [Any] the value to be transformed through #toString
     */
    fun appendHeader(header: String, vararg values: Any): Request {
        headers.append(header, listOf(values))
        return this
    }

    /**
     * Append each pair, using the key as header name and value as header content
     *
     * @param pairs [Pair<String, Any>]
     */
    fun appendHeader(vararg pairs: Pair<String, Any>): Request {
        pairs.forEach { pair -> appendHeader(pair.first, pair.second) }
        return this
    }

    fun body(body: ByteArray): Request {
        bodyCallback = { _, outputStream, _ ->
            outputStream?.write(body)
            body.size.toLong()
        }
        return this
    }

    fun body(body: String, charset: Charset = Charsets.UTF_8): Request = body(body.toByteArray(charset))

    fun jsonBody(body: String, charset: Charset = Charsets.UTF_8): Request {
        this[Headers.CONTENT_TYPE] = "application/json"
        return body(body, charset)
    }

    fun progress(handler: (readBytes: Long, totalBytes: Long) -> Unit): Request {
        val taskRequest = taskRequest
        when (taskRequest) {
            is DownloadTaskRequest -> {
                taskRequest.progressCallback = handler
            }
            is UploadTaskRequest -> {
                taskRequest.progressCallback = handler
            }
            else -> throw IllegalStateException("progress is only used with RequestType.DOWNLOAD or RequestType.UPLOAD")
        }
        return this
    }

    /**
     *  Replace each pair, using the key as header name and value as header content
     */
    fun blobs(blobs: (Request, URL) -> Iterable<Blob>): Request {
        val uploadTaskRequest = taskRequest as? UploadTaskRequest
                ?: throw IllegalStateException("source is only used with RequestType.UPLOAD")
        uploadTaskRequest.sourceCallback = blobs
        return this
    }

    fun blob(blob: (Request, URL) -> Blob) = blobs { request, _ -> listOf(blob(request, request.url)) }

    fun authenticate(username: String, password: String) = basicAuthentication(username, password)

    fun basicAuthentication(username: String, password: String): Request {
        val auth = "$username:$password"
        val encodedAuth = auth.encodeBase64ToString()
        this[Headers.AUTHORIZATION] = "Basic $encodedAuth"
        return this
    }

    fun bearerAuthentication(bearerToken: String): Request {
        this[Headers.AUTHORIZATION] = "Bearer $bearerToken"
        return this
    }

    fun dataParts(dataParts: (Request, URL) -> Iterable<DataPart>): Request {
        val uploadTaskRequest = taskRequest as? UploadTaskRequest
                ?: throw IllegalStateException("source is only used with RequestType.UPLOAD")
        val parts = dataParts(request, request.url)

        mediaTypes.apply {
            clear()
            addAll(parts.map { it.type })
        }

        names.apply {
            clear()
            addAll(parts.map { it.name })
        }

        uploadTaskRequest.sourceCallback = { _, _ ->
            parts.map { (file) -> Blob(file.name, file.length(), file::inputStream) }
        }

        return this
    }

    fun sources(sources: (Request, URL) -> Iterable<File>): Request {
        mediaTypes.clear()
        names.clear()

        val uploadTaskRequest = taskRequest as? UploadTaskRequest
                ?: throw IllegalStateException("source is only used with RequestType.UPLOAD")
        val files = sources(request, request.url)

        uploadTaskRequest.sourceCallback = { _, _ ->
            files.map { Blob(it.name, it.length(), it::inputStream) }
        }

        return this
    }

    fun source(source: (Request, URL) -> File): Request {
        sources { request, _ ->
            listOf(source(request, request.url))
        }

        return this
    }

    fun name(name: () -> String): Request {
        this.name = name()
        return this
    }

    fun destination(destination: (Response, URL) -> File): Request {
        val downloadTaskRequest = taskRequest as? DownloadTaskRequest
                ?: throw IllegalStateException("destination is only used with RequestType.DOWNLOAD")

        downloadTaskRequest.destinationCallback = destination
        return this
    }

    fun interrupt(interrupt: (Request) -> Unit): Request {
        taskRequest.apply {
            interruptCallback = interrupt
        }
        return this
    }

    fun allowRedirects(allowRedirects: Boolean): Request {
        isAllowRedirects = allowRedirects
        return this
    }

    fun submit(callable: Callable<*>) {
        taskFuture = executor.submit(callable)
    }

    fun callback(f: () -> Unit) {
        callbackExecutor.execute {
            f.invoke()
        }
    }

    fun cancel() {
        taskFuture?.cancel(true)
    }

    override val request: Request get() = this

    /**
     * Returns a string representation of the request.
     *
     * @see httpString
     * @see cUrlString
     *
     * @return [String] the string representation
     */
    override fun toString(): String = buildString {
        appendln("--> $url")
        appendln("\"Body : ${if (getHttpBody().isNotEmpty()) String(getHttpBody()) else "(empty)"}\"")
        appendln("\"Headers : (${headers.size})\"")

        val appendHeaderWithValue = { key: String, value: String -> appendln("$key : $value") }
        headers.transformIterate(appendHeaderWithValue)
    }

    /**
     * Returns a representation that can be used over the HTTP protocol
     *
     * @see toString
     * @see cUrlString
     *
     * @return [String] the string representation
     */
    fun httpString(): String = buildString {
        // url
        val params = parameters.joinToString(separator = "&", prefix = "?") { "${it.first}=${it.second}" }
        appendln("${method.value} $url$params")
        appendln()
        // headers

        val appendHeaderWithValue = { key: String, value: String -> appendln("$key : $value") }
        headers.transformIterate(appendHeaderWithValue)

        // body
        appendln()
        appendln(String(getHttpBody()))
    }

    /**
     * Returns a representation that can be used with cURL
     *
     * @see toString
     * @see httpString
     *
     * @return [String] the string representation
     */
    fun cUrlString(): String = buildString {
        append("$ curl -i")

        // method
        if (method != Method.GET) {
            append(" -X $method")
        }

        // body
        val escapedBody = String(getHttpBody()).replace("\"", "\\\"")
        if (escapedBody.isNotEmpty()) {
            append(" -d \"$escapedBody\"")
        }

        // headers
        val appendHeaderWithValue = { key: String, value: String -> append(" -H \"$key:$value\"") }
        headers.transformIterate(appendHeaderWithValue)

        // url
        append(" $url")
    }

    // byte array
    fun response(handler: (Request, Response, Result<ByteArray, FuelError>) -> Unit) =
            response(byteArrayDeserializer(), handler)

    fun response(handler: Handler<ByteArray>) = response(byteArrayDeserializer(), handler)

    fun response() = response(byteArrayDeserializer())

    // string
    fun responseString(charset: Charset = Charsets.UTF_8, handler: (Request, Response, Result<String, FuelError>) -> Unit) =
            response(stringDeserializer(charset), handler)

    fun responseString(charset: Charset, handler: Handler<String>) = response(stringDeserializer(charset), handler)

    fun responseString(handler: Handler<String>) = response(stringDeserializer(), handler)

    @JvmOverloads
    fun responseString(charset: Charset = Charsets.UTF_8) = response(stringDeserializer(charset))

    // object
    fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>, handler: (Request, Response, Result<T, FuelError>) -> Unit) = response(deserializer, handler)

    fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>, handler: Handler<T>) = response(deserializer, handler)

    fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>) = response(deserializer)

    companion object {
        fun byteArrayDeserializer() = ByteArrayDeserializer()

        fun stringDeserializer(charset: Charset = Charsets.UTF_8) = StringDeserializer(charset)
    }
}
