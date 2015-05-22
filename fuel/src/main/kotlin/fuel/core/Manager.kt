package fuel.core

import fuel.Fuel
import fuel.toolbox.HttpClient
import fuel.util.build
import fuel.util.readWriteLazy
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.properties.Delegates

/**
 * Created by Kittinun Vantasin on 5/14/15.
 */

public class Manager {

    var client: Client by Delegates.notNull()
    var basePath: String? = null
    var additionalHeaders: Map<String, String>? = null

    companion object Shared {

        var sharedInstance by Delegates.readWriteLazy {
            build(Manager()) {
                this.client = HttpClient()
            }
        }

        private var executor = Executors.newScheduledThreadPool(2 * Runtime.getRuntime().availableProcessors())

        fun submit(request: Request): Response {
            return sharedInstance.client.executeRequest(request)
        }

        fun <T> submit(call: Callable<T>) {
            executor.submit(call)
        }

    }

    fun request(method: Method, path: String, param: Map<String, Any?>? = null): Request {
        return request(build(Encoding()) {
            httpMethod = method
            baseUrlString = basePath
            urlString = path
            parameters = param
        })
    }

    fun request(method: Method, convertible: Fuel.PathStringConvertible, param: Map<String, Any?>? = null): Request {
        return request(build(Encoding()) {
            httpMethod = method
            baseUrlString = basePath
            urlString = convertible.path
            parameters = param
        })
    }

    fun request(convertible: Fuel.RequestConvertible): Request {
        return convertible.request
    }

}
