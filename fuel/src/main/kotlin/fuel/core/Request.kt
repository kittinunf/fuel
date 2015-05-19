package fuel.core

import fuel.util.build
import fuel.util.readWriteLazy
import java.net.URL
import java.net.URLEncoder
import java.util.ArrayList
import java.util.concurrent.Callable
import kotlin.properties.Delegates

/**
 * Created by Kittinun Vantasin on 5/13/15.
 */

public class Request {

    var httpMethod: Method = Method.GET
    var path: String by Delegates.notNull()
    val url: URL by Delegates.lazy { URL(path) }

    val timeoutInMillisecond = 1500

    val task: TaskRequest

    init {
        task = TaskRequest(this)
    }

    //interfaces
    public fun validate(statusCodeRange: IntRange): Request {
        build(task) {
            validator = { response ->
                statusCodeRange.contains(response.httpStatusCode)
            }
        }
        return this
    }

    public fun response(handler: (Request, Response?) -> Unit) {
        build(task) {
            successCallback = { response ->
                handler(this@Request, response)
            }

            failureCallback = { exception, response ->
                handler(this@Request, response)
            }
        }

        Manager.submit(task)
    }

    public fun response(handler: (Request, Response?, Either<Exception, ByteArray>) -> Unit) {
        build(task) {
            successCallback = { response ->
                handler(this@Request, response, Right(response.data))
            }

            failureCallback = { exception, response ->
                handler(this@Request, response, Left(exception))
            }
        }

        Manager.submit(task)
    }

    public fun responseString(handler: (Request, Response?, Either<Exception, String>) -> Unit) {
        build(task) {
            successCallback = { response ->
                handler(this@Request, response, Right(String(response.data)))
            }

            failureCallback = { exception, response ->
                handler(this@Request, response, Left(exception))

            }
        }

        Manager.submit(task)
    }

    companion object {

        class TaskRequest(val request: Request) : Callable<Unit> {

            var successCallback: ((Response) -> Unit)? = null
            var failureCallback: ((Exception, Response) -> Unit)? = null

            var validator: (Response) -> Boolean = { response ->
                (200..299).contains(response.httpStatusCode)
            }

            override fun call() {
                try {
                    val response = Manager.submit(request)
                    dispatchCallback(response)
                } catch (error: FuelError) {
                    failureCallback?.invoke(error.exception, error.response)
                }
            }

            fun dispatchCallback(response: Response) {
                //validate
                if (validator.invoke(response)) {
                    successCallback?.invoke(response)
                } else {
                    failureCallback?.invoke(IllegalStateException("Validation failed"), response)
                }
            }

        }

    }

}
