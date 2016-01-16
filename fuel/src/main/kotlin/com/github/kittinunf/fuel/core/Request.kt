package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.core.requests.DownloadTaskRequest
import com.github.kittinunf.fuel.core.requests.TaskRequest
import com.github.kittinunf.fuel.core.requests.UploadTaskRequest
import com.github.kittinunf.fuel.util.Base64
import com.github.kittinunf.fuel.util.readWriteLazy
import java.io.File
import java.net.URL
import java.nio.charset.Charset
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSocketFactory

class Request {
    enum class Type {
        REQUEST,
        DOWNLOAD,
        UPLOAD
    }

    val timeoutInMillisecond = 15000
    var syncMode = false

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

    var taskFuture: Future<*>? = null

    //configuration
    var socketFactory: SSLSocketFactory? = null
    var hostnameVerifier: HostnameVerifier? = null

    //callers
    lateinit var executor: ExecutorService
    lateinit var callbackExecutor: Executor

    fun sync(): Request {
        syncMode = true
        return this
    }

    //interfaces
    fun header(vararg pairs: Pair<String, Any>?): Request {
        pairs.forEach {
            if (it != null)
                httpHeaders.plusAssign(Pair(it.first, it.second.toString()))
        }
        return this
    }

    fun header(pairs: Map<String, Any>?): Request {
        if (pairs != null) {
            for ((key, value) in pairs) {
                header(key to value)
            }
        }
        return this
    }

    fun body(body: ByteArray): Request {
        httpBody = body
        return this
    }

    fun body(body: String, charset: Charset = Charsets.UTF_8): Request = body(body.toByteArray(charset))

    fun authenticate(username: String, password: String): Request {
        val auth = "$username:$password"
        val encodedAuth = Base64.encode(auth.toByteArray(), Base64.NO_WRAP)
        return header("Authorization" to "Basic " + String(encodedAuth))
    }

    fun validate(statusCodeRange: IntRange): Request {
        taskRequest.apply {
            validator = { response ->
                statusCodeRange.contains(response.httpStatusCode)
            }
        }
        return this
    }

    fun progress(handler: (readBytes: Long, totalBytes: Long) -> Unit): Request {
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

    fun source(source: (Request, URL) -> File): Request {
        val uploadTaskRequest = taskRequest as? UploadTaskRequest ?: throw IllegalStateException("source is only used with RequestType.UPLOAD")

        uploadTaskRequest.apply {
            sourceCallback = source
        }
        return this
    }

    fun destination(destination: (Response, URL) -> File): Request {
        val downloadTaskRequest = taskRequest as? DownloadTaskRequest ?: throw IllegalStateException("destination is only used with RequestType.DOWNLOAD")

        downloadTaskRequest.apply {
            destinationCallback = destination
        }
        return this
    }

    fun interrupt(interrupt: (Request) -> Unit): Request {
        taskRequest.apply {
            interruptCallback = interrupt
        }
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


    fun cUrlString(): String {
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
}
