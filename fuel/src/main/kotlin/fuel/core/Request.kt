package fuel.core

import fuel.util.build
import fuel.util.copyToWithProgress
import fuel.util.readWriteLazy
import org.apache.commons.codec.binary.Base64
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.HashMap
import java.util.concurrent.Callable
import kotlin.properties.Delegates

/**
 * Created by Kittinun Vantasin on 5/13/15.
 */

public class Request {

    enum class Type {
        REQUEST
        DOWNLOAD
        UPLOAD
    }

    val timeoutInMillisecond = 15000

    var requestType: Type = Type.REQUEST
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
        when (requestType) {
            Type.REQUEST -> TaskRequest(this)
            Type.DOWNLOAD -> DownloadTaskRequest(this)
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

    public fun progress(handler: (Long, Long) -> Unit): Request {
        val downloadTaskRequest = taskRequest as DownloadTaskRequest

        build(downloadTaskRequest) {
            progressCallback = handler
        }

        return this
    }

    public fun authenticate(username: String, password: String): Request {
        val auth = "$username:$password"
        val encodedAuth = Base64.encodeBase64(auth.toByteArray())
        return header("Authorization" to "Basic " + String(encodedAuth))
    }

    public fun validate(statusCodeRange: IntRange): Request {
        build(taskRequest) {
            validator = { response ->
                statusCodeRange.contains(response.httpStatusCode)
            }
        }
        return this
    }

    public fun response(handler: (Request, Response, Either<FuelError, ByteArray>) -> Unit) {
        build(taskRequest) {
            successCallback = { response ->
                handler(this@Request, response, Right(response.data))
            }

            failureCallback = { error, response ->
                handler(this@Request, response, Left(error))
            }
        }

        Manager.submit(taskRequest)
    }

    public fun responseString(handler: (Request, Response, Either<FuelError, String>) -> Unit) {
        build(taskRequest) {
            successCallback = { response ->
                handler(this@Request, response, Right(String(response.data)))
            }

            failureCallback = { error, response ->
                handler(this@Request, response, Left(error))

            }
        }

        Manager.submit(taskRequest)
    }

    public fun responseDestination(destination: (Response, URL) -> File, handler: (Request, Response, Either<FuelError, ByteArray>) -> Unit) {
        val downloadTaskRequest = taskRequest as DownloadTaskRequest

        build(downloadTaskRequest) {
            destinationCallback = destination

            successCallback = { response ->
                handler(this@Request, response, Right(ByteArray(0)))
            }

            failureCallback = { error, response ->
                handler(this@Request, response, Left(error))
            }
        }

        Manager.submit(downloadTaskRequest)
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
                    errorDataStream = response.dataStream
                }
                failureCallback?.invoke(error, response)
            }
        }

    }

    class DownloadTaskRequest(override val request: Request) : TaskRequest(request) {

        var progressCallback: ((Long, Long) -> Unit)? = null
        var destinationCallback: ((Response, URL) -> File)? = null

        override fun call() {
            try {
                val response = Manager.sharedInstance.client.executeRequest(request)
                val fileLocation = destinationCallback?.invoke(response, request.url)!!
                val fileOutputStream = FileOutputStream(fileLocation)
                response.dataStream?.copyToWithProgress(fileOutputStream) { readBytes ->
                    progressCallback?.invoke(readBytes, response.httpContentLength)
                }
                fileOutputStream.close()
                dispatchCallback(response)
            } catch(error: FuelError) {
                failureCallback?.invoke(error, error.response)
            }
        }
    }

}
