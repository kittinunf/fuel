package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.util.Base64
import com.github.kittinunf.fuel.util.copyTo
import com.github.kittinunf.fuel.util.readWriteLazy
import com.github.kittinunf.fuel.util.toHexString
import com.github.kittinunf.result.Result
import java.io.*
import java.net.URL
import java.net.URLConnection
import java.nio.charset.Charset
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSocketFactory

/**
 * Created by Kittinun Vantasin on 5/13/15.
 */

public class Request {

    enum class Type {
        REQUEST,
        DOWNLOAD,
        UPLOAD
    }

    val timeoutInMillisecond = 15000

    var type: Type = Type.REQUEST
    lateinit var httpMethod: Method
    lateinit var path: String
    lateinit var url: URL
    var httpBody: ByteArray = ByteArray(0)

    var httpHeaders by readWriteLazy {
        val additionalHeaders = Manager.instance.baseHeaders
        val headers = hashMapOf<String, String>()
        if (additionalHeaders != null) {
            headers.putAll(additionalHeaders)
        }
        return@readWriteLazy headers
    }

    //underlying task request
    val taskRequest: TaskRequest by lazy {
        when (type) {
            Type.DOWNLOAD -> DownloadTaskRequest(this)
            Type.UPLOAD -> UploadTaskRequest(this)
            else -> TaskRequest(this)
        }
    }

    private var taskFuture: Future<Unit>? = null

    //configuration
    var socketFactory: SSLSocketFactory? = null
    var hostnameVerifier: HostnameVerifier? = null

    //callers
    lateinit var executor: ExecutorService
    lateinit var callbackExecutor: Executor

    companion object {

        public fun byteArrayDeserializer(): Deserializable<ByteArray> {
            return object : Deserializable<ByteArray> {
                override fun deserialize(response: Response): ByteArray {
                    return response.data
                }
            }
        }

        public fun stringDeserializer(): Deserializable<String> {
            return object : Deserializable<String> {
                override fun deserialize(response: Response): String {
                    return String(response.data)
                }
            }
        }
    }

    //interfaces
    public fun header(pair: Pair<String, Any>?): Request {
        if (pair != null) {
            httpHeaders.plusAssign(Pair(pair.first, pair.second.toString()))
        }
        return this
    }

    public fun header(pairs: Map<String, Any>?): Request {
        if (pairs != null) {
            for ((key, value) in pairs) {
                header(key to value)
            }
        }
        return this
    }

    public fun body(body: ByteArray): Request {
        httpBody = body
        return this
    }

    public fun body(body: String, charset: Charset = Charsets.UTF_8): Request = body(body.toByteArray(charset))

    public fun authenticate(username: String, password: String): Request {
        val auth = "$username:$password"
        val encodedAuth = Base64.encode(auth.toByteArray(), Base64.NO_WRAP)
        return header("Authorization" to "Basic " + String(encodedAuth))
    }

    public fun validate(statusCodeRange: IntRange): Request {
        taskRequest.apply {
            validator = { response ->
                statusCodeRange.contains(response.httpStatusCode)
            }
        }
        return this
    }

    public fun progress(handler: (readBytes: Long, totalBytes: Long) -> Unit): Request {
        if (taskRequest as? DownloadTaskRequest != null) {
            val download = taskRequest as DownloadTaskRequest
            download.apply {
                progressCallback = handler
            }
        } else if (taskRequest as? UploadTaskRequest != null) {
            val upload = taskRequest as UploadTaskRequest
            upload.apply {
                progressCallback = handler
            }
        } else {
            throw IllegalStateException("progress is only used with RequestType.DOWNLOAD or RequestType.UPLOAD")
        }

        return this
    }

    public fun source(source: (Request, URL) -> File): Request {
        val uploadTaskRequest = taskRequest as? UploadTaskRequest ?: throw IllegalStateException("source is only used with RequestType.UPLOAD")

        uploadTaskRequest.apply {
            sourceCallback = source
        }
        return this
    }

    public fun destination(destination: (Response, URL) -> File): Request {
        val downloadTaskRequest = taskRequest as? DownloadTaskRequest ?: throw IllegalStateException("destination is only used with RequestType.DOWNLOAD")

        downloadTaskRequest.apply {
            destinationCallback = destination
        }
        return this
    }

    public fun interrupt(interrupt: (Request) -> Unit): Request {
        taskRequest.apply {
            interruptCallback = interrupt
        }
        return this
    }

    fun submit(callable: Callable<Unit>) {
        taskFuture = executor.submit(callable)
    }

    fun callback(f: () -> Unit) {
        callbackExecutor.execute {
            f.invoke()
        }
    }

    public fun cancel() {
        taskFuture?.cancel(true)
    }

    //byte array
    public fun response(handler: (Request, Response, Result<ByteArray, FuelError>) -> Unit) =
            response(byteArrayDeserializer(), handler)

    public fun response(handler: Handler<ByteArray>) = response(byteArrayDeserializer(), handler)

    //string
    public fun responseString(handler: (Request, Response, Result<String, FuelError>) -> Unit) =
            response(stringDeserializer(), handler)

    public fun responseString(handler: Handler<String>) = response(stringDeserializer(), handler)

    //object
    public fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>, handler: (Request, Response, Result<T, FuelError>) -> Unit) = response(deserializer, handler)

    public fun <T : Any> responseObject(deserializer: ResponseDeserializable<T>, handler: Handler<T>) = response(deserializer, handler)

    public fun cUrlString(): String {
        val elements = arrayListOf("$ curl -i")

        //method
        if (!httpMethod.equals(Method.GET)) {
            elements.add("-X $httpMethod")
        }

        //body
        val escapedBody = String(httpBody).replace("\"", "\\\"")
        if (escapedBody.isNotEmpty()) {
            elements.add("-d \"$escapedBody\"")
        }

        //headers
        for ((key, value) in httpHeaders) {
            elements.add("-H \"$key:$value\"")
        }

        //url
        elements.add("\"${url.toString()}\"")

        return elements.joinToString(" ").toString()
    }

    override fun toString(): String {
        val elements = arrayListOf("--> $httpMethod (${url.toString()})")

        //body
        elements.add("Body : ${ if (httpBody.size != 0) String(httpBody) else "(empty)"}")

        //headers
        elements.add("Headers : (${httpHeaders.size})")
        for ((key, value) in httpHeaders) {
            elements.add("$key : $value")
        }

        return elements.joinToString("\n").toString()
    }

    //underlying requests
    open class TaskRequest(open val request: Request) : Callable<Unit> {

        var successCallback: ((Response) -> Unit)? = null
        var failureCallback: ((FuelError, Response) -> Unit)? = null
        var interruptCallback: ((Request) -> Unit)? = null

        var validator: (Response) -> Boolean = { response ->
            (200..299).contains(response.httpStatusCode)
        }

        override fun call() {
            try {
                val response = Manager.instance.client.executeRequest(request)

                //dispatch
                dispatchCallback(response)
            } catch(error: FuelError) {
                if (error.exception as? InterruptedIOException != null) {
                    interruptCallback?.invoke(request)
                } else {
                    val response = Response()
                    response.url = request.url
                    failureCallback?.invoke(error, response)
                }
            }
        }

        fun dispatchCallback(response: Response) {
            //validate
            if (validator.invoke(response)) {
                successCallback?.invoke(response)
            } else {
                val error = FuelError().apply {
                    exception = HttpException(response.httpStatusCode, response.httpResponseMessage)
                    errorData = response.data
                }
                failureCallback?.invoke(error, response)
            }
        }

    }

    class DownloadTaskRequest(override val request: Request) : TaskRequest(request) {

        val BUFFER_SIZE = 1024

        var progressCallback: ((Long, Long) -> Unit)? = null
        lateinit var destinationCallback: ((Response, URL) -> File)

        lateinit var dataStream: InputStream
        lateinit var fileOutputStream: FileOutputStream

        override fun call() {
            try {
                val response = Manager.instance.client.executeRequest(request)

                val file = destinationCallback.invoke(response, request.url)

                //file output
                fileOutputStream = FileOutputStream(file)

                dataStream = ByteArrayInputStream(response.data)
                dataStream.copyTo(fileOutputStream, BUFFER_SIZE) { readBytes ->
                    progressCallback?.invoke(readBytes, response.httpContentLength)
                }

                //dispatch
                dispatchCallback(response)
            } catch(error: FuelError) {
                if (error.exception as? InterruptedIOException != null) {
                    interruptCallback?.invoke(request)
                } else {
                    val response = Response()
                    response.url = request.url
                    failureCallback?.invoke(error, response)
                }
            } catch(ex: Exception) {
                val error = FuelError().apply {
                    exception = ex
                }
                val response = Response()
                response.url = request.url
                failureCallback?.invoke(error, response)
            } finally {
                dataStream.close()
                fileOutputStream.close()
            }
        }

    }

    class UploadTaskRequest(override val request: Request) : TaskRequest(request) {

        val BUFFER_SIZE = 1024

        val CRLF = "\\r\\n"
        val boundary = System.currentTimeMillis().toHexString()

        var progressCallback: ((Long, Long) -> Unit)? = null
        lateinit var sourceCallback: ((Request, URL) -> File)

        lateinit var dataStream: ByteArrayOutputStream
        lateinit var fileInputStream: FileInputStream

        override fun call() {
            try {
                val file = sourceCallback.invoke(request, request.url)

                //file input
                fileInputStream = FileInputStream(file)

                dataStream = ByteArrayOutputStream().apply {
                    write(("--" + boundary + CRLF).toByteArray())
                    write(("Content-Disposition: form-data; filename=\"" + file.name + "\"").toByteArray())
                    write(CRLF.toByteArray())
                    write(("Content-Type: " + URLConnection.guessContentTypeFromName(file.name)).toByteArray())
                    write(CRLF.toByteArray())
                    write(CRLF.toByteArray())
                    flush()

                    //input file data
                    fileInputStream.copyTo(this, BUFFER_SIZE) { writtenBytes ->
                        progressCallback?.invoke(writtenBytes, file.length())
                    }

                    write(CRLF.toByteArray())
                    flush()
                    write(("--$boundary--").toByteArray())
                    write(CRLF.toByteArray())
                    flush()
                }

                request.body(dataStream.toByteArray())

                val response = Manager.instance.client.executeRequest(request)

                //dispatch
                dispatchCallback(response)
            } catch(error: FuelError) {
                if (error.exception as? InterruptedIOException != null) {
                    interruptCallback?.invoke(request)
                } else {
                    val response = Response()
                    response.url = request.url
                    failureCallback?.invoke(error, response)
                }
            } catch(ex: Exception) {
                val error = FuelError().apply {
                    exception = ex
                }
                val response = Response()
                response.url = request.url
                failureCallback?.invoke(error, response)
            } finally {
                dataStream.close()
                fileInputStream.close()
            }
        }

    }

}
