package fuel.core

import fuel.util.build
import fuel.util.copyTo
import fuel.util.readWriteLazy
import fuel.util.toHexString
import java.io.*
import java.net.Authenticator
import java.net.PasswordAuthentication
import java.net.URL
import java.net.URLConnection
import java.util.HashMap
import java.util.concurrent.Callable
import kotlin.properties.Delegates

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
    var httpMethod: Method by Delegates.notNull()
    var path: String by Delegates.notNull()
    var url: URL by Delegates.notNull()
    var httpBody: ByteArray? = null

    var httpHeaders by Delegates.readWriteLazy {
        val additionalHeaders = Manager.sharedInstance.additionalHeaders
        val headers = HashMap<String, String>()
        if (additionalHeaders != null) {
            headers.putAll(additionalHeaders)
        }
        return@readWriteLazy headers
    }

    //underlying task request
    val taskRequest: TaskRequest by Delegates.lazy {
        when (type) {
            Type.DOWNLOAD -> DownloadTaskRequest(this)
            Type.UPLOAD -> UploadTaskRequest(this)
            else -> TaskRequest(this)
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

    public fun authenticate(username: String, password: String): Request {
        Authenticator.setDefault(object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication? {
                return PasswordAuthentication(username, password.toCharArray())
            }
        })
        return this
    }

    public fun validate(statusCodeRange: IntRange): Request {
        build(taskRequest) {
            validator = { response ->
                statusCodeRange.contains(response.httpStatusCode)
            }
        }
        return this
    }

    public fun progress(handler: (readBytes: Long, totalBytes: Long) -> Unit): Request {
        if (taskRequest as? DownloadTaskRequest != null) {
            val download = taskRequest as DownloadTaskRequest
            build(download) {
                progressCallback = handler
            }
        } else if (taskRequest as? UploadTaskRequest != null) {
            val upload = taskRequest as UploadTaskRequest
            build(upload) {
                progressCallback = handler
            }
        } else {
            throw IllegalStateException("progress is only used with RequestType.DOWNLOAD or RequestType.UPLOAD")
        }

        return this
    }

    public fun source(source: (Request, URL) -> File): Request {
        val uploadTaskRequest = taskRequest as? UploadTaskRequest ?: throw IllegalStateException("source is only used with RequestType.UPLOAD")

        build(uploadTaskRequest) {
            sourceCallback = source
        }
        return this
    }

    public fun destination(destination: (Response, URL) -> File): Request {
        val downloadTaskRequest = taskRequest as? DownloadTaskRequest ?: throw IllegalStateException("destination is only used with RequestType.DOWNLOAD")

        build(downloadTaskRequest) {
            destinationCallback = destination
        }
        return this
    }

    public fun response(handler: (Request, Response, Either<FuelError, ByteArray>) -> Unit) {
        build(taskRequest) {
            successCallback = { response ->
                callback {
                    handler(this@Request, response, Right(response.data))
                }
            }

            failureCallback = { error, response ->
                callback {
                    handler(this@Request, response, Left(error))
                }
            }
        }

        Manager.executor.submit(taskRequest)
    }

    public fun response(handler: Handler<ByteArray>) {
        build(taskRequest) {
            successCallback = { response ->
                callback {
                    handler.success(this@Request, response, response.data)
                }
            }

            failureCallback = { error, response ->
                callback {
                    handler.failure(this@Request, response, error)
                }
            }
        }

        Manager.executor.submit(taskRequest)
    }

    public fun responseString(handler: (Request, Response, Either<FuelError, String>) -> Unit) {
        build(taskRequest) {
            successCallback = { response ->
                val data = String(response.data)
                callback {
                    handler(this@Request, response, Right(data))
                }
            }

            failureCallback = { error, response ->
                callback {
                    handler(this@Request, response, Left(error))
                }
            }
        }

        Manager.executor.submit(taskRequest)
    }

    public fun responseString(handler: Handler<String>) {
        build(taskRequest) {
            successCallback = { response ->
                val data = String(response.data)
                callback {
                    handler.success(this@Request, response, data)
                }
            }

            failureCallback = { error, response ->
                callback {
                    handler.failure(this@Request, response, error)
                }
            }
        }

        Manager.executor.submit(taskRequest)
    }

    //privates
    private fun callback(f: () -> Unit) {
        Manager.callbackExecutor.execute {
            f.invoke()
        }
    }

    open class TaskRequest(open val request: Request) : Callable<Unit> {

        var successCallback: ((Response) -> Unit)? = null
        var failureCallback: ((FuelError, Response) -> Unit)? = null

        var validator: (Response) -> Boolean = { response ->
            (200..299).contains(response.httpStatusCode)
        }

        override fun call() {
            try {
                val response = Manager.sharedInstance.client.executeRequest(request)

                //dispatch
                dispatchCallback(response)
            } catch (error: FuelError) {
                failureCallback?.invoke(error, error.response)
            }
        }

        fun dispatchCallback(response: Response) {
            //validate
            if (validator.invoke(response)) {
                successCallback?.invoke(response)
            } else {
                val error = build(FuelError()) {
                    exception = IllegalStateException("Validation failed")
                    errorData = response.data
                }
                failureCallback?.invoke(error, response)
            }
        }

    }

    class DownloadTaskRequest(override val request: Request) : TaskRequest(request) {

        val BUFFER_SIZE = 1024

        var progressCallback: ((Long, Long) -> Unit)? = null
        var destinationCallback: ((Response, URL) -> File)? = null

        var dataStream: InputStream by Delegates.notNull()
        var fileOutputStream: FileOutputStream by Delegates.notNull()

        override fun call() {
            try {
                val response = Manager.sharedInstance.client.executeRequest(request)
                val file = destinationCallback?.invoke(response, request.url)!!

                //file output
                fileOutputStream = FileOutputStream(file)

                dataStream = ByteArrayInputStream(response.data)
                dataStream.copyTo(fileOutputStream, BUFFER_SIZE) { readBytes ->
                    progressCallback?.invoke(readBytes, response.httpContentLength)
                }

                //dispatch
                dispatchCallback(response)
            } catch(error: FuelError) {
                failureCallback?.invoke(error, error.response)
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
        var sourceCallback: ((Request, URL) -> File) by Delegates.notNull()

        var dataStream: ByteArrayOutputStream by Delegates.notNull()
        var fileInputStream: FileInputStream by Delegates.notNull()

        override fun call() {
            try {
                val file = sourceCallback.invoke(request, request.url)

                //file input
                fileInputStream = FileInputStream(file)

                dataStream = build(ByteArrayOutputStream()) {
                    write(("--" + boundary + CRLF).toByteArray())
                    write(("Content-Disposition: form-data; filename=\"" + file.getName() + "\"").toByteArray())
                    write(CRLF.toByteArray())
                    write(("Content-Type: " + URLConnection.guessContentTypeFromName(file.getName())).toByteArray())
                    write(CRLF.toByteArray())
                    write(CRLF.toByteArray())
                    flush()

                    //input file data
                    fileInputStream.copyTo(this, BUFFER_SIZE) { writtenBytes ->
                        progressCallback?.invoke(writtenBytes, file.length())
                    }

                    write(CRLF.toByteArray())
                    flush()
                    write(("--" + boundary + "--").toByteArray())
                    write(CRLF.toByteArray())
                    flush()
                }

                request.body(dataStream.toByteArray())

                val response = Manager.sharedInstance.client.executeRequest(request)

                //dispatch
                dispatchCallback(response)
            } catch(error: FuelError) {
                failureCallback?.invoke(error, error.response)
            } finally {
                dataStream.close()
                fileInputStream.close()
            }
        }

    }

}
