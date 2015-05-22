package fuel.core

import fuel.util.build
import fuel.util.readWriteLazy
import org.apache.commons.codec.binary.Base64
import java.net.URL
import java.util.concurrent.Callable
import kotlin.properties.Delegates

/**
 * Created by Kittinun Vantasin on 5/13/15.
 */

public class Request {

    val timeoutInMillisecond = 15000

    var httpMethod: Method by Delegates.notNull()
    var path: String by Delegates.notNull()
    var url: URL by Delegates.notNull()
    var httpBody: ByteArray? = null

    var httpHeaders by Delegates.readWriteLazy {
        val headers = hashMapOf("Accept-Encoding" to "compress;q=0.5, gzip;q=1.0")
        val additionalHeaders = Manager.sharedInstance.additionalHeaders

        if (additionalHeaders != null) {
            headers += additionalHeaders
        }
        return@readWriteLazy headers
    }

    val task: TaskRequest

    init {
        task = TaskRequest(this)
    }

    //interfaces
    public fun header(pair: Pair<String, Any>?): Request {
        if (pair != null) {
            httpHeaders.plusAssign(Pair(pair.first, pair.second.toString()))
        }
        return this
    }

    public fun authenticate(username: String, password: String): Request {
        val auth = "$username:$password"
        val encodedAuth = Base64.encodeBase64(auth.toByteArray())
        return header("Authorization" to "Basic " + String(encodedAuth))
    }

    public fun validate(statusCodeRange: IntRange): Request {
        build(task) {
            validator = { response ->
                statusCodeRange.contains(response.httpStatusCode)
            }
        }
        return this
    }

    public fun response(handler: (Request, Response, Either<FuelError, ByteArray>) -> Unit) {
        build(task) {
            successCallback = { response ->
                handler(this@Request, response, Right(response.data))
            }

            failureCallback = { error, response ->
                handler(this@Request, response, Left(error))
            }
        }

        Manager.submit(task)
    }

    public fun responseString(handler: (Request, Response, Either<FuelError, String>) -> Unit) {
        build(task) {
            successCallback = { response ->
                handler(this@Request, response, Right(String(response.data)))
            }

            failureCallback = { error, response ->
                handler(this@Request, response, Left(error))

            }
        }

        Manager.submit(task)
    }

    companion object {

        open class TaskRequest(open val request: Request) : Callable<Unit> {

            var successCallback: ((Response) -> Unit)? = null
            var failureCallback: ((FuelError, Response) -> Unit)? = null

            var validator: (Response) -> Boolean = { response ->
                (200..299).contains(response.httpStatusCode)
            }

            override fun call() {
                try {
                    val response = Manager.submit(request)
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

        }

    }

}
