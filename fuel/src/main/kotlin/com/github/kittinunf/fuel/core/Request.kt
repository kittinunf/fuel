package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.core.requests.CancellableRequest
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.charset.Charset
import kotlin.reflect.KClass

typealias Parameters = List<Pair<String, Any?>>
typealias RequestFeatures = MutableMap<String, Request>
typealias Tags = MutableMap<KClass<*>, Any>

interface Request : RequestFactory.RequestConvertible {
    val method: Method
    var url: URL
    val headers: Headers
    var parameters: Parameters
    var executionOptions: RequestExecutionOptions
    val body: Body
    val enabledFeatures: RequestFeatures

    /**
     * Add a [ProgressCallback] tracking the [Body] of the [Request]
     *
     * @see body
     * @see com.github.kittinunf.fuel.core.requests.UploadRequest.progress
     *
     * @return self
     */
    fun requestProgress(handler: ProgressCallback): Request

    /**
     * Add a [ProgressCallback] tracking the [Body] of the [Response]
     *
     * @see com.github.kittinunf.fuel.core.requests.DownloadRequest.progress
     *
     * @return self
     */
    fun responseProgress(handler: ProgressCallback): Request

    /**
     * Overwrite the [Request] [timeout] in milliseconds
     *
     * @note [Client] must implement this behaviour
     * @note the default client sets [java.net.HttpURLConnection.setConnectTimeout]
     *
     * @param timeout [Int] timeout in milliseconds
     * @return self
     */
    fun timeout(timeout: Int): Request

    /**
     * Overwrite the [Request] [timeout] in milliseconds
     *
     * @note [Client] must implement this behaviour
     * @note the default client sets [java.net.HttpURLConnection.setReadTimeout]
     *
     * @param timeout [Int] timeout in milliseconds
     * @return self
     */
    fun timeoutRead(timeout: Int): Request

    /**
     * Overwrite [RequestExecutionOptions] http cache usage flag
     *
     * @note [Client] must implement this behaviour
     * @note The default client sends `Cache-Control: none` if this flag is false, defaults to true
     *
     * @see java.net.HttpURLConnection.setUseCaches
     * @param useHttpCache [Boolean] true if suggest client to allow cached responses, false otherwise
     */
    fun useHttpCache(useHttpCache: Boolean): Request

    /**
     * Follow redirects as handled by instances of RedirectInterceptors
     *  i.e. [com.github.kittinunf.fuel.core.interceptors.redirectResponseInterceptor]
     *
     * @param allowRedirects [Boolean] true if allowing, false if not
     * @return self
     */
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

    /**
     * Execute the [Request] asynchronously, using the [handler], into a [ByteArray]
     *
     * @param handler [ResponseResultHandler] the handler to report the [Request], [Response] and Result of [ByteArray]
     * @return [CancellableRequest] the request in flight
     */
    fun response(handler: ResponseResultHandler<ByteArray>): CancellableRequest

    /**
     * Execute the [Request] asynchronously, using the [handler], into a [ByteArray]
     *
     * @param handler [ResultHandler] the handler to report the Result of [ByteArray]
     * @return [CancellableRequest] the request in flight
     */
    fun response(handler: ResultHandler<ByteArray>): CancellableRequest

    /**
     * Execute the [Request] asynchronously, using the [handler], into a [ByteArray]
     *
     * @param handler [ResponseHandler] the handler to report the [Request], [Response], and [ByteArray] or [FuelError]
     * @return [CancellableRequest] the request in flight
     */
    fun response(handler: ResponseHandler<ByteArray>): CancellableRequest

    /**
     * Execute the [Request] asynchronously, using the [handler], into a [ByteArray]
     *
     * @param handler [Handler] the handler to report the [ByteArray] or [FuelError]
     * @return [CancellableRequest] the request in flight
     */
    fun response(handler: Handler<ByteArray>): CancellableRequest

    /**
     * Execute the [Request] asynchronously, using the [handler], into a [Charsets.UTF_8] [String]
     *
     * @param handler [ResponseResultHandler] the handler to report the [Request], [Response] and Result of [String]
     * @return [CancellableRequest] the request in flight
     */
    fun responseString(handler: ResponseResultHandler<String>): CancellableRequest

    /**
     * Execute the [Request] asynchronously, using the [handler], into a [String]
     *
     * @param charset [Charset] the charset to use for the [String]
     * @param handler [ResponseResultHandler] the handler to report the [Request], [Response] and Result of [String]
     * @return [CancellableRequest] the request in flight
     */
    fun responseString(charset: Charset = Charsets.UTF_8, handler: ResponseResultHandler<String>): CancellableRequest

    /**
     * Execute the [Request] asynchronously, using the [handler], into a [Charsets.UTF_8] [String]
     *
     * @param handler [ResultHandler] the handler to report the Result of [String]
     * @return [CancellableRequest] the request in flight
     */
    fun responseString(handler: ResultHandler<String>): CancellableRequest

    /**
     * Execute the [Request] asynchronously, using the [handler], into a [String]
     *
     * @param charset [Charset] the charset to use for the [String]
     * @param handler [ResultHandler] the handler to report the Result of [String]
     * @return [CancellableRequest] the request in flight
     */
    fun responseString(charset: Charset = Charsets.UTF_8, handler: ResultHandler<String>): CancellableRequest

    /**
     * Execute the [Request] asynchronously, using the [handler], into a [Charsets.UTF_8] [String]
     *
     * @param handler [ResponseHandler] the handler to report the [Request], [Response], and [String] or [FuelError]
     * @return [CancellableRequest] the request in flight
     */
    fun responseString(handler: ResponseHandler<String>): CancellableRequest

    /**
     * Execute the [Request] asynchronously, using the [handler], into a [String]
     *
     * @param charset [Charset] the charset to use for the [String]
     * @param handler [ResponseHandler] the handler to report the [Request], [Response], and [String] or [FuelError]
     * @return [CancellableRequest] the request in flight
     */
    fun responseString(charset: Charset = Charsets.UTF_8, handler: ResponseHandler<String>): CancellableRequest

    /**
     * Execute the [Request] asynchronously, using the [handler], into a [Charsets.UTF_8] [String]
     *
     * @param handler [Handler] the handler to report the [String] or [FuelError]
     * @return [CancellableRequest] the request in flight
     */
    fun responseString(handler: Handler<String>): CancellableRequest

    /**
     * Execute the [Request] asynchronously, using the [handler], into a [String]

     * @param charset [Charset] the charset to use for the [String]
     * @param handler [Handler] the handler to report the [String] or [FuelError]
     * @return [CancellableRequest] the request in flight
     */
    fun responseString(charset: Charset = Charsets.UTF_8, handler: Handler<String>): CancellableRequest

    /**
     * Execute the [Request] asynchronously, using the [handler], into a [T]
     *
     * @param deserializer [ResponseDeserializable] instance that can turn [Response] into [T]
     * @param handler [ResponseResultHandler] the handler to report the [Request], [Response] and Result of [T]
     * @return [CancellableRequest] the request in flight
     */
    fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>, handler: ResponseResultHandler<T>): CancellableRequest

    /**
     * Execute the [Request] asynchronously, using the [handler], into a [T]
     *
     * @param deserializer [ResponseDeserializable] instance that can turn [Response] into [T]
     * @param handler [ResponseHandler] the handler to report the [Request], [Response], and [T] or [FuelError]
     * @return [CancellableRequest] the request in flight
     */
    fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>, handler: ResponseHandler<T>): CancellableRequest

    /**
     * Execute the [Request] asynchronously, using the [handler], into a [T]
     *
     * @param deserializer [ResponseDeserializable] instance that can turn [Response] into [T]
     * @param handler [ResultHandler] the handler to report the Result of [T]
     * @return [CancellableRequest] the request in flight
     */
    fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>, handler: ResultHandler<T>): CancellableRequest

    /**
     * Execute the [Request] asynchronously, using the [handler], into a [T]
     *
     * @param deserializer [ResponseDeserializable] instance that can turn [Response] into [T]
     * @param handler [Handler] the handler to report the [T] or [FuelError]
     * @return [CancellableRequest] the request in flight
     */
    fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>, handler: Handler<T>): CancellableRequest

    /**
     * Execute the [Request] synchronously, into a [ByteArray]
     *
     * @note this is a synchronous execution and can not be cancelled
     *
     * @return [ResponseResultOf] the response result of [ByteArray]
     */
    fun response(): ResponseResultOf<ByteArray>

    /**
     * Execute the [Request] synchronously, into a [String]
     *
     * @note this is a synchronous execution and can not be cancelled
     *
     * @param charset [Charset] the character set to use for the string
     * @return [ResponseResultOf] the response result of [String]
     */
    fun responseString(charset: Charset = Charsets.UTF_8): ResponseResultOf<String>

    /**
     * Execute the [Request] synchronously, into a [Charsets.UTF_8] [String]
     *
     * @note this is a synchronous execution and can not be cancelled
     *
     * @return [ResponseResultOf] the response result of [String]
     */
    fun responseString(): ResponseResultOf<String>

    /**
     * Execute the [Request] synchronously, into a [T]
     *
     * @note this is a synchronous execution and can not be cancelled
     *
     * @param deserializer [ResponseDeserializable] instance that can turn the [Response] into a [T]
     * @return [ResponseResultOf] the response result of [T]
     */
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
     * @param repeatable [Boolean] loads the body into memory upon reading
     *
     * @return [Request] the request
     */
    fun body(openStream: BodySource, calculateLength: BodyLength? = null, charset: Charset = Charsets.UTF_8, repeatable: Boolean = false): Request

    /**
     * Sets the body from a generic stream
     *
     * @note the stream will be read from the position it's at. Make sure you rewind it if you want it to be read from
     *  the start.
     *
     * @param stream [InputStream] a stream to read from
     * @param calculateLength [Number?] size in bytes if it is known
     * @param charset [Charset] the charset to write with
     * @param repeatable [Boolean] loads the body into memory upon reading
     *
     * @return [Request] the request
     */
    fun body(stream: InputStream, calculateLength: BodyLength? = null, charset: Charset = Charsets.UTF_8, repeatable: Boolean = false): Request

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

    /**
     * Add a [InterruptCallback] to the [RequestExecutionOptions]
     *
     * @see RequestExecutionOptions.interruptCallbacks
     *
     * @return self
     */
    fun interrupt(interrupt: InterruptCallback): Request

    /**
     * Override a default [ResponseValidator] to the [RequestExecutionOptions]
     *
     * @see RequestExecutionOptions.responseValidator
     *
     * @return self
     */
    fun validate(validator: ResponseValidator): Request

    /**
     * Attach tag to the request
     *
     * @note tag is a generic purpose tagging for Request. This can be used to attach arbitrarily object to the Request instance.
     * @note Tags internally is represented as hashMap that uses class as a key.
     *
     * @param t [Any]
     * @return [Request] the modified request
     */
    fun tag(t: Any): Request

    /**
     * Return corresponding tag from the request
     *
     * @note tag is a generic purpose tagging for Request. This can be used to attach arbitrarily object to the Request instance.
     * @note Tags internally is represented as hashMap that uses class as a key.
     *
     * @param clazz [KClass]
     * @return [Any] previously attached tag if any, null otherwise
     */
    fun <T : Any> getTag(clazz: KClass<T>): T?
}
