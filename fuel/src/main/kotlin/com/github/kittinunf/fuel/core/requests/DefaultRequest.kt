package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.Body
import com.github.kittinunf.fuel.core.BodyLength
import com.github.kittinunf.fuel.core.BodySource
import com.github.kittinunf.fuel.core.Handler
import com.github.kittinunf.fuel.core.HeaderValues
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.InterruptCallback
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Parameters
import com.github.kittinunf.fuel.core.ProgressCallback
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.RequestExecutionOptions
import com.github.kittinunf.fuel.core.RequestFeatures
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.core.ResponseHandler
import com.github.kittinunf.fuel.core.ResponseResultHandler
import com.github.kittinunf.fuel.core.ResponseValidator
import com.github.kittinunf.fuel.core.ResultHandler
import com.github.kittinunf.fuel.core.Tags
import com.github.kittinunf.fuel.core.deserializers.ByteArrayDeserializer
import com.github.kittinunf.fuel.core.deserializers.StringDeserializer
import com.github.kittinunf.fuel.core.response
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.URL
import java.net.URLConnection
import java.nio.charset.Charset
import kotlin.reflect.KClass

data class DefaultRequest(
    override val method: Method,
    override var url: URL,
    override val headers: Headers = Headers(),
    override var parameters: Parameters = listOf(),
    internal var _body: Body = DefaultBody(),
    override val enabledFeatures: RequestFeatures = mutableMapOf(),
    private val tags: Tags = mutableMapOf()
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
     * @param repeatable [Boolean] loads the body into memory upon reading
     *
     * @return [Request] the request
     */
    override fun body(openStream: BodySource, calculateLength: BodyLength?, charset: Charset, repeatable: Boolean): Request {
        _body = DefaultBody
            .from(openStream = openStream, calculateLength = calculateLength, charset = charset)
            .let { body -> if (repeatable) body.asRepeatable() else body }
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
     * @param repeatable [Boolean] loads the body into memory upon reading
     *
     * @return [Request] the request
     */
    override fun body(stream: InputStream, calculateLength: BodyLength?, charset: Charset, repeatable: Boolean) =
        body(openStream = { stream }, calculateLength = calculateLength, charset = charset, repeatable = repeatable)

    /**
     * Sets the body from a byte array
     *
     * @param bytes [ByteArray] the bytes to write
     * @param charset [Charset] the charset to write with
     * @return [Request] the request
     */
    override fun body(bytes: ByteArray, charset: Charset) =
        body(stream = ByteArrayInputStream(bytes), calculateLength = { bytes.size.toLong() }, charset = charset, repeatable = true)

    /**
     * Sets the body from a string
     *
     * @param body [String] the string to write
     * @param charset [Charset] the charset to write with
     * @return [Request] the request
     */
    override fun body(body: String, charset: Charset): Request =
        body(bytes = body.toByteArray(charset), charset = charset)
            .let {
                if (header(Headers.CONTENT_TYPE).lastOrNull().isNullOrBlank())
                header(Headers.CONTENT_TYPE, "text/plain; charset=${charset.name()}")
                else it
            }

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
    }.let {
        if (header(Headers.CONTENT_TYPE).lastOrNull().isNullOrBlank()) {
            val contentType = URLConnection.guessContentTypeFromName(file.name)
            header(Headers.CONTENT_TYPE, "$contentType; charset=${charset.name()}")
        } else {
            it
        }
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

    /**
     * Add a [ProgressCallback] tracking the [Body] of the [Request]
     *
     * @see body
     * @see com.github.kittinunf.fuel.core.requests.UploadRequest.progress
     *
     * @return self
     */
    override fun requestProgress(handler: ProgressCallback): Request {
        executionOptions.requestProgress += handler
        return request
    }

    /**
     * Add a [ProgressCallback] tracking the [Body] of the [com.github.kittinunf.fuel.core.Response]
     *
     * @see com.github.kittinunf.fuel.core.requests.DownloadRequest.progress
     *
     * @return self
     */
    override fun responseProgress(handler: ProgressCallback): Request {
        executionOptions.responseProgress += handler
        return request
    }

    /**
     * Add a [InterruptCallback] to the [RequestExecutionOptions]
     *
     * @see RequestExecutionOptions.interruptCallbacks
     *
     * @return self
     */
    override fun interrupt(interrupt: InterruptCallback) = request.also {
        it.executionOptions.interruptCallbacks.plusAssign(interrupt)
    }

    /**
     * Overwrite the [Request] [timeout] in milliseconds
     *
     * @note [com.github.kittinunf.fuel.core.Client] must implement this behaviour
     * @note the default client sets [java.net.HttpURLConnection.setConnectTimeout]
     *
     * @param timeout [Int] timeout in milliseconds
     * @return self
     */
    override fun timeout(timeout: Int) = request.also {
        it.executionOptions.timeoutInMillisecond = timeout
    }

    /**
     * Overwrite the [Request] [timeout] in milliseconds
     *
     * @note [com.github.kittinunf.fuel.core.Client] must implement this behaviour
     * @note the default client sets [java.net.HttpURLConnection.setReadTimeout]
     *
     * @param timeout [Int] timeout in milliseconds
     * @return self
     */
    override fun timeoutRead(timeout: Int) = request.also {
        it.executionOptions.timeoutReadInMillisecond = timeout
    }

    /**
     * Follow redirects as handled by instances of RedirectInterceptors
     *  i.e. [com.github.kittinunf.fuel.core.interceptors.redirectResponseInterceptor]
     *
     * @note The interceptor must implement this behaviour
     * @note The provided RedirectResponseInterceptor defaults to true
     *
     * @param allowRedirects [Boolean] true if allowing, false if not
     * @return self
     */
    override fun allowRedirects(allowRedirects: Boolean) = request.also {
        it.executionOptions.allowRedirects = allowRedirects
    }

    /**
     * Overwrite [RequestExecutionOptions] http cache usage flag
     *
     * @note [com.github.kittinunf.fuel.core.Client] must implement this behaviour
     * @note The default client sends `Cache-Control: none` if this flag is false, defaults to true
     *
     * @see java.net.HttpURLConnection.setUseCaches
     * @param useHttpCache [Boolean] true if suggest client to allow cached responses, false otherwise
     */
    override fun useHttpCache(useHttpCache: Boolean) = request.also {
        it.executionOptions.useHttpCache = useHttpCache
    }

    /**
     * Overwrite [RequestExecutionOptions] response validator block
     *
     * @note The default responseValidator is to throw [com.github.kittinunf.fuel.core.HttpException]
     * @note if the response http status code is not in the range of (100 - 399) which should consider as failure response
     *
     * @param validator [ResponseValidator]
     * @return [Request] the modified request
     */
    override fun validate(validator: ResponseValidator) = request.also {
        it.executionOptions.responseValidator = validator
    }

    /**
     * Attach tag to the request
     *
     * @note tag is a generic purpose tagging for Request. This can be used to attach arbitrarily object to the Request instance.
     * @note Tags internally is represented as hashMap that uses class as a key.
     *
     * @param t [Any]
     * @return [Request] the modified request
     */
    override fun tag(t: Any) = request.also {
        tags[t::class] = t
    }

    /**
     * Return corresponding tag from the request
     *
     * @note tag is a generic purpose tagging for Request. This can be used to attach arbitrarily object to the Request instance.
     * @note Tags internally is represented as hashMap that uses class as a key.
     *
     * @param clazz [KClass]
     * @return [Any] previously attached tag if any, null otherwise
     */
    override fun <T : Any> getTag(clazz: KClass<T>) = tags[clazz] as? T

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
        appendln("--> $method $url")
        appendln("Body : ${body.asString(header(Headers.CONTENT_TYPE).lastOrNull())}")
        appendln("Headers : (${headers.size})")

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

    override fun responseString() = response(StringDeserializer(Charsets.UTF_8))

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
