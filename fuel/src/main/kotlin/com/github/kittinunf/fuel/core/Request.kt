package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.deserializers.ByteArrayDeserializer
import com.github.kittinunf.fuel.core.deserializers.StringDeserializer
import com.github.kittinunf.fuel.core.requests.CancellableRequest
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
typealias RequestFeatures = MutableMap<String, Request>

interface Request : Fuel.RequestConvertible {
    val method: Method
    val url: URL
    val headers: Headers
    val parameters: Parameters
    var executionOptions: RequestExecutionOptions
    val body: Body
    val enabledFeatures: RequestFeatures

    fun requestProgress(handler: ProgressCallback): Request
    fun responseProgress(handler: ProgressCallback): Request
    fun timeout(timeout: Int): Request
    fun timeoutRead(timeout: Int): Request
    fun useHttpCache(useHttpCache: Boolean): Request
    fun allowRedirects(allowRedirects: Boolean): Request

    /**
     * Returns a string representation of the request.
     *
     * @see com.github.kittinunf.fuel.core.extensions.httpString
     * @see com.github.kittinunf.fuel.core.extensions.cUrlString
     *
     * @return [String] the string representation
     */
    override fun toString(): String

    /**
     * Get the current values of the header, after normalisation of the header
     * @param header [String] the header name
     * @return the current values (or empty if none)
     */
    operator fun get(header: String): HeaderValues

    /**
     * Set the values of the header, overriding what's there, after normalisation of the header
     *
     * @param header [String] the header name
     * @param values [Collection<*>] the values to be transformed through #toString
     * @return self
     */
    operator fun set(header: String, values: Collection<*>): Request

    /**
     * Set the value of the header, overriding what's there, after normalisation of the header
     *
     * @param header [String] the header name
     * @param value [Any] the value to be transformed through #toString
     */
    operator fun set(header: String, value: Any): Request

    /**
     * Get the current values
     *
     * @see get(header: String)
     * @return [HeaderValues] the current values
     */
    fun header(header: String): HeaderValues

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
    fun header(map: Map<String, Any>): Request

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
    fun header(vararg pairs: Pair<String, Any>): Request

    /**
     * Replace the header with the provided values
     *
     * @see set(header: String, values: Collection<*>)
     *
     * @param header [String] the header to set
     * @param values [List<Any>] the values to set the header to
     * @return [Request] the modified request
     */
    fun header(header: String, values: Collection<*>): Request

    /**
     * Replace the header with the provided value
     *
     * @see set(header: String, values: List<Any>)
     *
     * @param header [String] the header to set
     * @param value [Any] the value to set the header to
     * @return [Request] the modified request
     */
    fun header(header: String, value: Any): Request

    /**
     * Replace the header with the provided values
     *
     * @see set(header: String, values: List<Any>)
     *
     * @param header [String] the header to set
     * @param values [Any] the values to set the header to
     * @return [Request] the modified request
     */
    fun header(header: String, vararg values: Any): Request

    /**
     * Appends the value to the header or sets it if there was none yet
     *
     * @param header [String] the header name to append to
     * @param value [Any] the value to be transformed through #toString
     */
    fun appendHeader(header: String, value: Any): Request

    /**
     * Appends the value to the header or sets it if there was none yet
     *
     * @param header [String] the header name to append to
     * @param values [Any] the value to be transformed through #toString
     */
    fun appendHeader(header: String, vararg values: Any): Request

    /**
     * Append each pair, using the key as header name and value as header content
     *
     * @param pairs [Pair<String, Any>]
     */
    fun appendHeader(vararg pairs: Pair<String, Any>): Request

    fun response(handler: ResponseResultHandler<ByteArray>): CancellableRequest
    fun response(handler: ResultHandler<ByteArray>): CancellableRequest
    fun response(handler: ResponseHandler<ByteArray>): CancellableRequest
    fun response(handler: Handler<ByteArray>): CancellableRequest
    fun responseString(handler: ResponseResultHandler<String>): CancellableRequest
    fun responseString(charset: Charset = Charsets.UTF_8, handler: ResponseResultHandler<String>): CancellableRequest
    fun responseString(handler: ResultHandler<String>): CancellableRequest
    fun responseString(charset: Charset = Charsets.UTF_8, handler: ResultHandler<String>): CancellableRequest
    fun responseString(handler: ResponseHandler<String>): CancellableRequest
    fun responseString(charset: Charset = Charsets.UTF_8, handler: ResponseHandler<String>): CancellableRequest
    fun responseString(handler: Handler<String>): CancellableRequest
    fun responseString(charset: Charset = Charsets.UTF_8, handler: Handler<String>): CancellableRequest
    fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>, handler: ResponseResultHandler<T>): CancellableRequest
    fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>, handler: ResponseHandler<T>): CancellableRequest
    fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>, handler: ResultHandler<T>): CancellableRequest
    fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>, handler: Handler<T>): CancellableRequest

    fun response(): ResponseResultOf<ByteArray>
    fun responseString(charset: Charset = Charsets.UTF_8): ResponseResultOf<String>
    fun responseString(): ResponseResultOf<String>
    fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>): ResponseResultOf<T>

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
    fun body(openStream: BodySource, calculateLength: BodyLength? = null, charset: Charset = Charsets.UTF_8): Request

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
    fun body(stream: InputStream, calculateLength: BodyLength? = null, charset: Charset = Charsets.UTF_8): Request

    /**
     * Sets the body from a byte array
     *
     * @param bytes [ByteArray] the bytes to write
     * @param charset [Charset] the charset to write with
     * @return [Request] the request
     */
    fun body(bytes: ByteArray, charset: Charset = Charsets.UTF_8): Request

    /**
     * Sets the body from a string
     *
     * @param body [String] the string to write
     * @param charset [Charset] the charset to write with
     * @return [Request] the request
     */
    fun body(body: String, charset: Charset = Charsets.UTF_8): Request

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
    fun body(file: File, charset: Charset = Charsets.UTF_8): Request

    /**
     * Sets the body to a defined [Body]
     *
     * @param body [Body] the body to assign
     * @return [Request] the request
     */
    fun body(body: Body): Request

    fun interrupt(interrupt: (Request) -> Unit): Request
}

data class RequestExecutionOptions(
    val client: Client,
    val socketFactory: SSLSocketFactory? = null,
    val hostnameVerifier: HostnameVerifier? = null,
    val executorService: ExecutorService,
    val callbackExecutor: Executor,
    val requestTransformer: RequestTransformer,
    var responseTransformer: ResponseTransformer
) {
    val requestProgress: Progress = Progress()
    val responseProgress: Progress = Progress()
    var timeoutInMillisecond: Int = 15_000
    var timeoutReadInMillisecond: Int = 15_000
    var decodeContent: Boolean? = null
    var allowRedirects: Boolean? = null
    var useHttpCache: Boolean? = null
    var interruptCallback: ((Request) -> Unit)? = null

    fun callback(f: () -> Unit) = callbackExecutor.execute(f)
    fun <T> submit(task: Callable<T>): Future<T> = executorService.submit(task)

    fun transformResponse(next: ResponseTransformer): RequestExecutionOptions {
        val previous = responseTransformer
        responseTransformer = { request, response -> next(request, previous(request, response)) }
        return this
    }
}

data class DefaultRequest(
    override val method: Method,
    override val url: URL,
    override val headers: Headers = Headers(),
    override val parameters: Parameters = listOf(),
    internal var _body: Body = DefaultBody(),
    override val enabledFeatures: RequestFeatures = mutableMapOf()
) : Request {

    override lateinit var executionOptions: RequestExecutionOptions
    override val body: Body get() = _body

    /**
     * Get the current values of the header, after normalisation of the header
     * @param header [String] the header name
     * @return the current values (or empty if none)
     */
    override operator fun get(header: String): HeaderValues {
        return headers[header]
    }

    /**
     * Set the values of the header, overriding what's there, after normalisation of the header
     *
     * @param header [String] the header name
     * @param values [Collection<*>] the values to be transformed through #toString
     * @return self
     */
    override operator fun set(header: String, values: Collection<*>): Request {
        headers[header] = values.map { it.toString() }
        return request
    }

    /**
     * Set the value of the header, overriding what's there, after normalisation of the header
     *
     * @param header [String] the header name
     * @param value [Any] the value to be transformed through #toString
     */
    override operator fun set(header: String, value: Any): Request {
        when (value) {
            is Collection<*> -> this[header] = value
            else -> headers[header] = value.toString()
        }
        return request
    }

    /**
     * Get the current values
     *
     * @see get(header: String)
     * @return [HeaderValues] the current values
     */
    override fun header(header: String) = get(header)

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
    override fun header(map: Map<String, Any>): Request {
        headers.putAll(Headers.from(map))
        return request
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
    override fun header(vararg pairs: Pair<String, Any>): Request {
        headers.putAll(Headers.from(*pairs))
        return request
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
    override fun header(header: String, values: Collection<*>) = set(header, values)

    /**
     * Replace the header with the provided value
     *
     * @see set(header: String, values: List<Any>)
     *
     * @param header [String] the header to set
     * @param value [Any] the value to set the header to
     * @return [Request] the modified request
     */
    override fun header(header: String, value: Any): Request = set(header, value)

    /**
     * Replace the header with the provided values
     *
     * @see set(header: String, values: List<Any>)
     *
     * @param header [String] the header to set
     * @param values [Any] the values to set the header to
     * @return [Request] the modified request
     */
    override fun header(header: String, vararg values: Any) = set(header, values.toList())

    /**
     * Appends the value to the header or sets it if there was none yet
     *
     * @param header [String] the header name to append to
     * @param value [Any] the value to be transformed through #toString
     */
    override fun appendHeader(header: String, value: Any): Request {
        headers.append(header, value)
        return request
    }

    /**
     * Appends the value to the header or sets it if there was none yet
     *
     * @param header [String] the header name to append to
     * @param values [Any] the value to be transformed through #toString
     */
    override fun appendHeader(header: String, vararg values: Any): Request {
        headers.append(header, values.toList())
        return request
    }

    /**
     * Append each pair, using the key as header name and value as header content
     *
     * @param pairs [Pair<String, Any>]
     */
    override fun appendHeader(vararg pairs: Pair<String, Any>): Request {
        pairs.forEach { pair -> appendHeader(pair.first, pair.second) }
        return request
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
    override fun body(openStream: BodySource, calculateLength: BodyLength?, charset: Charset): Request {
        _body = DefaultBody.from(openStream = openStream, calculateLength = calculateLength, charset = charset)
        return request
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
    override fun body(stream: InputStream, calculateLength: BodyLength?, charset: Charset) =
        body({ stream }, calculateLength, charset)

    /**
     * Sets the body from a byte array
     *
     * @param bytes [ByteArray] the bytes to write
     * @param charset [Charset] the charset to write with
     * @return [Request] the request
     */
    override fun body(bytes: ByteArray, charset: Charset) =
        body(ByteArrayInputStream(bytes), { bytes.size.toLong() }, charset)

    /**
     * Sets the body from a string
     *
     * @param body [String] the string to write
     * @param charset [Charset] the charset to write with
     * @return [Request] the request
     */
    override fun body(body: String, charset: Charset): Request =
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
    override fun body(file: File, charset: Charset): Request = when (charset) {
        Charsets.UTF_8 -> body({ FileInputStream(file) }, { file.length() }, charset)
        else -> body({ FileInputStream(file) }, null, charset)
    }

    /**
     * Sets the body to a defined [Body]
     *
     * @param body [Body] the body to assign
     * @return [Request] the request
     */
    override fun body(body: Body): Request {
        _body = body
        return request
    }

    override fun requestProgress(handler: ProgressCallback): Request {
        executionOptions.requestProgress += handler
        return request
    }

    override fun responseProgress(handler: ProgressCallback): Request {
        executionOptions.responseProgress += handler
        return request
    }

    override fun interrupt(interrupt: (Request) -> Unit) = request.also {
        it.executionOptions.interruptCallback = interrupt
    }

    override fun timeout(timeout: Int) = request.also {
        it.executionOptions.timeoutInMillisecond = timeout
    }

    override fun timeoutRead(timeout: Int) = request.also {
        it.executionOptions.timeoutReadInMillisecond = timeout
    }

    override fun allowRedirects(allowRedirects: Boolean) = request.also {
        it.executionOptions.allowRedirects = allowRedirects
    }

    override fun useHttpCache(useHttpCache: Boolean) = request.also {
        it.executionOptions.useHttpCache = useHttpCache
    }

    override val request: Request get() = this

    /**
     * Returns a string representation of the request.
     *
     * @see com.github.kittinunf.fuel.core.extensions.httpString
     * @see com.github.kittinunf.fuel.core.extensions.cUrlString
     *
     * @return [String] the string representation
     */
    override fun toString(): String = buildString {

        val bodyString = when {
            body.isEmpty() -> "(empty)"
            body.isConsumed() -> "(consumed)"
            else -> String(body.toByteArray())
        }

        appendln("--> $method $url")
        appendln("\"Body : $bodyString\"")
        appendln("\"Headers : (${headers.size})\"")

        val appendHeaderWithValue = { key: String, value: String -> appendln("$key : $value") }
        headers.transformIterate(appendHeaderWithValue)
    }

    override fun response(handler: ResponseResultHandler<ByteArray>) =
        response(ByteArrayDeserializer(), handler)
    override fun response(handler: ResultHandler<ByteArray>) =
        response(ByteArrayDeserializer(), handler)
    override fun response(handler: ResponseHandler<ByteArray>) =
        response(ByteArrayDeserializer(), handler)
    override fun response(handler: Handler<ByteArray>) =
        response(ByteArrayDeserializer(), handler)
    override fun response() =
        response(ByteArrayDeserializer())

    override fun responseString(charset: Charset, handler: ResponseResultHandler<String>) =
        response(StringDeserializer(charset), handler)
    override fun responseString(handler: ResponseResultHandler<String>) =
        responseString(Charsets.UTF_8, handler)
    override fun responseString(charset: Charset, handler: ResultHandler<String>) =
        response(StringDeserializer(charset), handler)
    override fun responseString(handler: ResultHandler<String>) =
            responseString(Charsets.UTF_8, handler)
    override fun responseString(charset: Charset, handler: ResponseHandler<String>) =
        response(StringDeserializer(charset), handler)
    override fun responseString(handler: ResponseHandler<String>) =
        response(StringDeserializer(), handler)
    override fun responseString(charset: Charset, handler: Handler<String>) =
        response(StringDeserializer(charset), handler)
    override fun responseString(handler: Handler<String>) =
        response(StringDeserializer(), handler)
    override fun responseString(charset: Charset) =
        response(StringDeserializer(charset))
    override fun responseString() =
        response(StringDeserializer(Charsets.UTF_8))

    override fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>, handler: ResponseResultHandler<T>) =
        response(deserializer, handler)
    override fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>, handler: ResultHandler<T>) =
        response(deserializer, handler)
    override fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>, handler: ResponseHandler<T>) =
        response(deserializer, handler)
    override fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>, handler: Handler<T>) =
        response(deserializer, handler)
    override fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>) =
        response(deserializer)
}
