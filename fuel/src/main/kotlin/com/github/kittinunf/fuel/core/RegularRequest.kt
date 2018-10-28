package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.core.deserializers.ByteArrayDeserializer
import com.github.kittinunf.fuel.core.deserializers.StringDeserializer
import com.github.kittinunf.fuel.core.executors.InterruptCallback
import com.github.kittinunf.fuel.core.executors.RequestExecutor
import com.github.kittinunf.fuel.core.requests.DownloadDestinationCallback
import com.github.kittinunf.fuel.core.requests.DownloadRequest
import com.github.kittinunf.fuel.core.requests.MultipartRequest
import com.github.kittinunf.fuel.util.encodeBase64ToString
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.URL
import java.nio.charset.Charset

data class RegularRequest(
    override val method: Method,
    val path: String,

    override val url: URL,
    override val headers: Headers = Headers(),
    override val parameters: Parameters = listOf(),
    override var body: Body = DefaultBody(),
    override val progress: Progress = Progress()
) : Request {
    override lateinit var executor: RequestExecutor

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
    override fun header(header: String, value: Any) = set(header, value)

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
        this.body = DefaultBody.from(openStream = openStream, calculateLength = calculateLength, charset = charset)
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
        body(ByteArrayInputStream(bytes), { bytes.size }, charset)

    /**
     * Sets the body from a string
     *
     * @param body [String] the string to write
     * @param charset [Charset] the charset to write with
     * @return [Request] the request
     */
    override fun body(body: String, charset: Charset): Request =
        body({ body.byteInputStream(charset) }, { charset.encode(body).limit() }, charset)

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
     * Set the body to a JSON string and automatically set the json content type
     */
    override fun jsonBody(body: String, charset: Charset): Request {
        this[Headers.CONTENT_TYPE] = "application/json"
        return body(body, charset)
    }

    override fun progress(handler: ProgressCallback): Request {
        progress.add(handler)
        return request
    }

    override fun progress(handlers: Progress): Request {
        progress.add(*handlers.handlers.toTypedArray())
        return request
    }

    override fun multipart() = MultipartRequest.from(this)
    override fun download() = DownloadRequest.from(this)
    override fun destination(destination: DownloadDestinationCallback) =
        download().destination(destination)

    override fun timeout(timeout: Int) = executor.timeout(timeout)
    override fun timeoutRead(timeout: Int) = executor.timeoutRead(timeout)
    override fun caching(value: Boolean?) = executor.caching(value)
    override fun followRedirects(value: Boolean?) = executor.followRedirects(value)
    override fun interrupt(callback: InterruptCallback) = executor.interrupt(callback)

    override fun authenticate(username: String, password: String) = basicAuthentication(username, password)

    override fun basicAuthentication(username: String, password: String): Request {
        val auth = "$username:$password"
        val encodedAuth = auth.encodeBase64ToString()
        this[Headers.AUTHORIZATION] = "Basic $encodedAuth"
        return request
    }

    override fun bearerAuthentication(token: String): Request {
        this[Headers.AUTHORIZATION] = "Bearer $token"
        return request
    }

    /**
     * This propety is overriden in decorators so it starts returning the decorated property
     */
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
    override fun httpString(): String = buildString {
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
    override fun cUrlString(): String = buildString {
        append("$ curl -i")

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

    override fun response(handler: HandlerWithResult<ByteArray>) =
        response(ByteArrayDeserializer(), handler)
    override fun response(handler: Handler<ByteArray>) =
        response(ByteArrayDeserializer(), handler)
    override fun response() =
        response(ByteArrayDeserializer())

    override fun responseString(charset: Charset, handler: HandlerWithResult<String>) =
        response(StringDeserializer(charset), handler)
    override fun responseString(charset: Charset, handler: Handler<String>) =
        response(StringDeserializer(charset), handler)
    override fun responseString(handler: Handler<String>) =
        response(StringDeserializer(), handler)

    override fun responseString(charset: Charset) =
        response(StringDeserializer(charset))

    override fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>, handler: HandlerWithResult<T>) =
        response(deserializer, handler)
    override fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>, handler: Handler<T>) =
        response(deserializer, handler)
    override fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>) =
        response(deserializer)
}