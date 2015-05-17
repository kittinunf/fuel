package fuel.core

import java.net.URL
import java.util.concurrent.Callable
import kotlin.properties.Delegates
import kotlin.test.expect

/**
 * Created by Kittinun Vantasin on 5/13/15.
 */

public class Request {

    var method: Method = Method.GET

    var urlString: String by Delegates.notNull()

    val url: URL by Delegates.lazy { URL(urlString) }

    val timeoutInMillisecond = 1500

    public fun response(handler: (Request, Response?) -> Unit) {
        val task = TaskRequest(this) {
            successCallback = { response ->
                handler(this@Request, response)
            }

            failureCallback = { exception, response ->
                handler(this@Request, response)
            }
        }

        Executor.submit(task)
    }

    public fun response(handler: (Request, Response?, Either<Exception, ByteArray>) -> Unit) {
        val task = TaskRequest(this) {
            successCallback = { response ->
                handler(this@Request, response, Right(response.data))
            }

            failureCallback = { exception, response ->
                handler(this@Request, response, Left(exception))
            }
        }

        Executor.submit(task)
    }

    public fun responseString(handler: (Request, Response?, Either<Exception, String>) -> Unit) {
        val task = TaskRequest(this) {
            successCallback = { response ->
                handler(this@Request, response, Right(String(response.data)))
            }

            failureCallback = { exception, response ->
                handler(this@Request, response, Left(exception))

            }
        }

        Executor.submit(task)
    }

    companion object {

        class TaskRequest(val request: Request) : Callable<Unit> {

            var successCallback: ((Response) -> Unit)? = null
            var failureCallback: ((Exception, Response?) -> Unit)? = null

            override fun call() {
                try {
                    val response = Manager.sharedInstance.client.executeRequest(request);
                    dispatchCallback(response)
                } catch (e: Exception) {
                    failureCallback?.invoke(e, null)
                }
            }

            fun dispatchCallback(response: Response) {
                //validate
                successCallback?.invoke(response)
            }

        }

        inline fun TaskRequest(request: Request, builder: TaskRequest.() -> Unit): TaskRequest {
            val taskRequest = TaskRequest(request)
            taskRequest.builder()
            return taskRequest
        }

    }

}

public inline fun Request(builder: Request.() -> Unit): Request {
    val request = Request()
    request.builder()
    return request
}
