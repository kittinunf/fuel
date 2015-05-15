package fuel.core

import java.net.URL
import java.util.concurrent.Callable
import kotlin.properties.Delegates

/**
 * Created by Kittinun Vantasin on 5/13/15.
 */

public class Request {

    var method: Method = Method.GET

    var urlString: String by Delegates.notNull()

    val url: URL by Delegates.lazy { URL(urlString) }

    val timeoutInMillisecond = 1500

    public fun response(handler: (Request, Response) -> Unit) {
        val task = TaskRequest()
        task.callback = { response ->
            handler(this@Request, response)
        }

        Executor.execute(task)
    }

    public fun response(handler: (Request, Response, Either<Exception, ByteArray>) -> Unit) {
        val task = TaskRequest()
        task.callback = { response ->
            handler(this@Request, response, Right(response.data))
        }

        Executor.execute(task)
    }

    public fun responseString(handler: (Request, Response, Either<Exception, String>) -> Unit) {
        val task = TaskRequest()
        task.callback = { response ->
            handler(this@Request, response, Right(String(response.data)))
        }

        Executor.execute(task)
    }

    //TODO: possibly change to static inner class?
    inner class TaskRequest : Runnable {

        var callback: ((Response) -> Unit)? = null

        override fun run() {
            val response = Manager.sharedInstance.client.executeRequest(this@Request)
            callback?.invoke(response)
        }
    }

}

public inline fun Request(builder: Request.() -> Unit): Request {
    val request = Request()
    request.builder()
    return request
}
