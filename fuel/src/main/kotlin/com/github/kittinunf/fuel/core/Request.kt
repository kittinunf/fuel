package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.deserializers.ByteArrayDeserializer
import com.github.kittinunf.fuel.core.deserializers.StringDeserializer
import com.github.kittinunf.fuel.core.requests.DownloadTaskRequest
import com.github.kittinunf.fuel.core.requests.TaskRequest
import com.github.kittinunf.fuel.core.requests.UploadTaskRequest
import com.github.kittinunf.fuel.util.Base64
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

class Request(
        val method: Method,
        val path: String,
        val url: URL,
        var type: Type = Type.REQUEST,
        val headers: MutableMap<String, String> = mutableMapOf(),
        val parameters: List<Pair<String, Any?>> = listOf(),
        var name: String = "",
        val names: MutableList<String> = mutableListOf(),
        val mediaTypes: MutableList<String> = mutableListOf(),
        var isAllowRedirects: Boolean = true,
        var timeoutInMillisecond: Int,
        var timeoutReadInMillisecond: Int) : Fuel.RequestConvertible {

    @Deprecated(replaceWith = ReplaceWith("method"), message = "http naming is deprecated, use 'method' instead")
    val httpMethod
        get() = method

    @Deprecated(replaceWith = ReplaceWith("headers"), message = "http naming is deprecated, use 'headers' instead")
    val httpHeaders
        get() = headers

    enum class Type {
        REQUEST,
        DOWNLOAD,
        UPLOAD
    }

    //body
    var bodyCallback: ((Request, OutputStream?, Long) -> Long)? = null

    private fun getHttpBody(): ByteArray = ByteArrayOutputStream().apply {
        bodyCallback?.invoke(request, this, 0)
    }.toByteArray()

    internal lateinit var client: Client

    //underlying task request
    internal val taskRequest: TaskRequest by lazy {
        when (type) {
            Type.DOWNLOAD -> DownloadTaskRequest(this)
            Type.UPLOAD -> UploadTaskRequest(this)
            else -> TaskRequest(this)
        }
    }

    private var taskFuture: Future<*>? = null

    //configuration
    internal var socketFactory: SSLSocketFactory? = null
    internal var hostnameVerifier: HostnameVerifier? = null

    //callers
    internal lateinit var executor: ExecutorService
    internal lateinit var callbackExecutor: Executor

    //interceptor
    internal var requestInterceptor: ((Request) -> Request)? = null
    internal var responseInterceptor: ((Request, Response) -> Response)? = null

    //interfaces
    fun timeout(timeout: Int): Request {
        timeoutInMillisecond = timeout
        return this
    }

    fun timeoutRead(timeout: Int): Request {
        timeoutReadInMillisecond = timeout
        return this
    }

    /**
     *  <p> Note that your value, will be converted to a String via the toString() </p>
     *  <p> Please note that header of the same key are supported and headers with the same key
     *  will be sent in the format of `key` : [ value; value; value ] </p>
     *
     * @param pairs This all the key value pairs you wish to add to the headers
     *
     * @return the request supplied
     *
     * */
    fun header(vararg pairs: Pair<String, Any>?): Request {
        pairs.filterNotNull().forEach { (key,value) ->
            if (!headers.containsKey(key)) {
                headers += Pair(key, value.toString())
            } else {
                headers[key] =  headers.getValue(key).let { "$it; $value" }
            }
        }
        return this
    }

    /**
     *  <p> Note that your value, will be converted to a String via the toString() </p>
     *
     *  <p> Please note that header of the same key are supported however as this function take a map
     *  multiple keys are not supported via this function as values assigned to the same key will be overwritten
     *  hence the last value that is written to that key will be the one used </p>
     *
     * @param pairs This all the key value pair you wish to add to the headers
     *
     * @return the request supplied
     *
     * */
    fun header(pairs: Map<String, Any>?): Request = header(pairs, true)

    internal fun header(pairs: Map<String, Any>?, replace: Boolean): Request {
        pairs?.forEach {
            if (replace || !headers.containsKey(it.key) ) {
                headers += Pair(it.key, it.value.toString())
            }
        }
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

    fun authenticate(username: String, password: String): Request {
        val auth = "$username:$password"
        val encodedAuth = Base64.encode(auth.toByteArray(), Base64.NO_WRAP)
        return header("Authorization" to "Basic " + String(encodedAuth))
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

    fun blobs(blobs: (Request, URL) -> Iterable<Blob>): Request {
        val uploadTaskRequest = taskRequest as? UploadTaskRequest
                ?: throw IllegalStateException("source is only used with RequestType.UPLOAD")
        uploadTaskRequest.sourceCallback = blobs

        return this
    }

    fun blob(blob: (Request, URL) -> Blob): Request {
        blobs { request, _ -> listOf(blob(request, request.url)) }
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

    override val request: Request
        get() = this

    override fun toString(): String = buildString {
        appendln("--> $url")
        appendln("\"Body : ${if (getHttpBody().isNotEmpty()) String(getHttpBody()) else "(empty)"}\"")
        appendln("\"Headers : (${headers.size})\"")
        for ((key, value) in headers) {
            appendln("$key : $value")
        }
    }

    fun httpString(): String = buildString {
        // url
        val params = parameters.joinToString(separator = "&", prefix = "?") { "${it.first}=${it.second}" }
        appendln("${method.value} $url$params")
        appendln()
        // headers
        for ((key, value) in headers) {
            appendln("$key : $value")
        }
        // body
        appendln()
        appendln(String(getHttpBody()))
    }

    fun cUrlString(): String = buildString {
        append("$ curl -i")

        //method
        if (method != Method.GET) {
            append(" -X $method")
        }

        //body
        val escapedBody = String(getHttpBody()).replace("\"", "\\\"")
        if (escapedBody.isNotEmpty()) {
            append(" -d \"$escapedBody\"")
        }

        //headers
        for ((key, value) in headers) {
            append(" -H \"$key:$value\"")
        }

        //url
        append(" $url")
    }

    //byte array
    fun response(handler: (Request, Response, Result<ByteArray, FuelError>) -> Unit) =
            response(byteArrayDeserializer(), handler)

    fun response(handler: Handler<ByteArray>) = response(byteArrayDeserializer(), handler)

    fun response() = response(byteArrayDeserializer())

    //string
    fun responseString(charset: Charset = Charsets.UTF_8, handler: (Request, Response, Result<String, FuelError>) -> Unit) =
            response(stringDeserializer(charset), handler)

    fun responseString(charset: Charset, handler: Handler<String>) = response(stringDeserializer(charset), handler)

    fun responseString(handler: Handler<String>) = response(stringDeserializer(), handler)

    @JvmOverloads
    fun responseString(charset: Charset = Charsets.UTF_8) = response(stringDeserializer(charset))

    //object
    fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>, handler: (Request, Response, Result<T, FuelError>) -> Unit) = response(deserializer, handler)

    fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>, handler: Handler<T>) = response(deserializer, handler)

    fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>) = response(deserializer)

    companion object {
        fun byteArrayDeserializer() = ByteArrayDeserializer()

        fun stringDeserializer(charset: Charset = Charsets.UTF_8) = StringDeserializer(charset)
    }

}
