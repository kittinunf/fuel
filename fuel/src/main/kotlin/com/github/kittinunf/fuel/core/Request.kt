package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.deserializers.ByteArrayDeserializer
import com.github.kittinunf.fuel.core.deserializers.JsonDeserializer
import com.github.kittinunf.fuel.core.deserializers.StringDeserializer
import com.github.kittinunf.fuel.core.requests.DownloadTaskRequest
import com.github.kittinunf.fuel.core.requests.TaskRequest
import com.github.kittinunf.fuel.core.requests.UploadSourceCallback
import com.github.kittinunf.fuel.core.requests.UploadTaskRequest
import com.github.kittinunf.fuel.util.encodeBase64ToString
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.URL
import java.nio.charset.Charset
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSocketFactory

private typealias RequestTransformer = (Request) -> Request
private typealias ResponseTransformer = (Request, Response) -> Response
typealias Parameters = List<Pair<String, Any?>>

class Request(
    val method: Method,
    val path: String,
    val url: URL,
    var type: Type = Type.REQUEST,
    val headers: Headers = Headers(),
    val parameters: Parameters = listOf(),
    var name: String = "file",
    val names: MutableList<String> = mutableListOf(),
    val mediaTypes: MutableList<String> = mutableListOf(),
    var isAllowRedirects: Boolean = true,
    var useHttpCache: Boolean? = null,
    var decodeContent: Boolean? = null,
    var timeoutInMillisecond: Int = 15_000,
    var timeoutReadInMillisecond: Int = 15_000,
    private val _responseProgress: Progress = Progress(),
    private val _requestProgress: Progress = Progress(),
    internal var _body: Body = DefaultBody()
) : Fuel.RequestConvertible {
    enum class Type {
        REQUEST,
        DOWNLOAD,
        UPLOAD
    }

    internal lateinit var client: Client

    val body: Body get() = _body
    val requestProgress: Progress get() = _requestProgress
    val responseProgress: Progress get() = _responseProgress

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
     * @param map [Map<String, Any>] map of headers to replace. Value can be a list or single value
     * @return [Request] the modified request
     */
    fun header(map: Map<String, Any>): Request {
        headers.putAll(Headers.from(map))
        return this
    }

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
     * @param pairs [Pair<String, Any>] map of headers to replace. Value can be a list or single value
     * @return [Request] the modified request
     */
    fun header(vararg pairs: Pair<String, Any>): Request {
        headers.putAll(Headers.from(*pairs))
        return this
    }

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
     * Replace the header with the provided values
     *
     * @see set(header: String, values: List<Any>)
     *
     * @param header [String] the header to set
     * @param values [Any] the values to set the header to
     * @return [Request] the modified request
     */
    fun header(header: String, vararg values: Any) = set(header, values.toList())

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
        headers.append(header, values.toList())
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

    /**
     * Sets the body to be read from a generic body source.
     *
     * @note in earlier versions the body callback would be called multiple times in order to maybe get the size. But
     *  that would lead to closed streams being unable to be read. If the size is known, set it before anything else.
     *
     * @param openStream [BodySource] a function that yields a stream
     * @param calculateLength [Number?] size in +bytes+ if it is known
     * @param charset [Charset] the charset to write with
     *
     * @return [Request] the request
     */
    fun body(openStream: BodySource, calculateLength: BodyLength? = null, charset: Charset = Charsets.UTF_8): Request {
        _body = DefaultBody.from(openStream = openStream, calculateLength = calculateLength, charset = charset)
        return this
    }

    /**
     * Sets the body from a generic stream
     *
     * @note the stream will be read from the position it's at. Make sure you rewind it if you want it to be read from
     *  the start.
     *
     * @param stream [InputStream] a stream to read from
     * @param calculateLength [Number?] size in bytes if it is known
     * @param charset [Charset] the charset to write with
     *
     * @return [Request] the request
     */
    fun body(stream: InputStream, calculateLength: BodyLength? = null, charset: Charset = Charsets.UTF_8) =
        body({ stream }, calculateLength, charset)

    /**
     * Sets the body from a byte array
     *
     * @param bytes [ByteArray] the bytes to write
     * @param charset [Charset] the charset to write with
     * @return [Request] the request
     */
    fun body(bytes: ByteArray, charset: Charset = Charsets.UTF_8) =
        body(ByteArrayInputStream(bytes), { bytes.size.toLong() }, charset)

    /**
     * Sets the body from a string
     *
     * @param body [String] the string to write
     * @param charset [Charset] the charset to write with
     * @return [Request] the request
     */
    fun body(body: String, charset: Charset = Charsets.UTF_8): Request =
        body(body.toByteArray(charset), charset)

    /**
     * Sets the body to the contents of a file.
     *
     * @note this does *NOT* make this a multipart upload. For that you can use the upload request. This function can be
     *  used if you want to upload the single contents of a text based file as an inline body.
     *
     * @note when charset is not UTF-8, this forces the client to use chunked encoding, because file.length() gives the
     *  length of the file in bytes without considering the charset. If the charset is to be considered, the file needs
     *  to be read in its entirety which defeats the purpose of using a file.
     *
     * @param file [File] the file to write to the body
     * @param charset [Charset] the charset to write with
     * @return [Request] the request
     */
    fun body(file: File, charset: Charset = Charsets.UTF_8): Request = when (charset) {
        Charsets.UTF_8 -> body({ FileInputStream(file) }, { file.length() }, charset)
        else -> body({ FileInputStream(file) }, null, charset)
    }

    /**
     * Set the body to a JSON string and automatically set the json content type
     */
    fun jsonBody(body: String, charset: Charset = Charsets.UTF_8): Request {
        this[Headers.CONTENT_TYPE] = "application/json"
        return body(body, charset)
    }

    fun progress(handler: ProgressCallback): Request {
        val taskRequest = taskRequest
        return when (taskRequest) {
            is DownloadTaskRequest -> responseProgress(handler)
            is UploadTaskRequest -> requestProgress(handler)
            else -> throw UnsupportedOperationException("Use requestProgress(handler) or responseProgress(handler)")
        }
    }

    fun requestProgress(handler: ProgressCallback): Request {
        _requestProgress += handler
        return this
    }

    fun responseProgress(handler: ProgressCallback): Request {
        _responseProgress += handler
        return this
    }
    /**
     *  Replace each pair, using the key as header name and value as header content
     */
    fun blobs(blobs: UploadSourceCallback): Request {
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
        callbackExecutor.execute { f() }
    }

    fun cancel(): Request {
        taskFuture?.cancel(true)
        return this
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

        val bodyString = when {
            body.isEmpty() -> "(empty)"
            body.isConsumed() -> "(consumed)"
            else -> String(body.toByteArray())
        }

        appendln("--> $url")
        appendln("\"Body : $bodyString\"")
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
        appendln(String(body.toByteArray()))
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
        append("curl -i")

        // method
        if (method != Method.GET) {
            append(" -X $method")
        }

        // body
        val escapedBody = String(body.toByteArray()).replace("\"", "\\\"")
        if (escapedBody.isNotEmpty()) {
            append(" -d \"$escapedBody\"")
        }

        // headers
        val appendHeaderWithValue = { key: String, value: String -> append(" -H \"$key:$value\"") }
        headers.transformIterate(appendHeaderWithValue)

        // url
        append(" $url")
    }

    fun response(handler: HandlerWithResult<ByteArray>) =
        response(ByteArrayDeserializer(), handler)
    fun response(handler: Handler<ByteArray>) =
        response(ByteArrayDeserializer(), handler)
    fun response() =
        response(ByteArrayDeserializer())

    fun responseString(charset: Charset = Charsets.UTF_8, handler: HandlerWithResult<String>) =
        response(StringDeserializer(charset), handler)
    fun responseString(charset: Charset, handler: Handler<String>) =
        response(StringDeserializer(charset), handler)
    fun responseString(handler: Handler<String>) =
        response(StringDeserializer(), handler)

    @JvmOverloads
    fun responseString(charset: Charset = Charsets.UTF_8) =
        response(StringDeserializer(charset))

    fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>, handler: HandlerWithResult<T>) =
        response(deserializer, handler)
    fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>, handler: Handler<T>) =
        response(deserializer, handler)
    fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>) =
            response(deserializer)

    fun Request.responseJson(handler: HandlerWithResult<Json>) -> Unit) =
        response(JsonDeserializer(), handler)
    fun Request.responseJson(handler: Handler<Json>) =
        response(JsonDeserializer(), handler)
    fun Request.responseJson() =
        response(JsonDeserializer())
}
