package fuel.core

import fuel.Fuel
import fuel.toolbox.HttpClient
import fuel.util.MainThreadExecutor
import fuel.util.build
import fuel.util.readWriteLazy
import java.util.concurrent.Executor
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

        //manager
        var sharedInstance by Delegates.readWriteLazy {
            build(Manager()) {
                this.client = HttpClient()
            }
        }

        //background executor
        var executor by Delegates.readWriteLazy {
            Executors.newCachedThreadPool { command ->
                Thread {
                    Thread.currentThread().setPriority(Thread.NORM_PRIORITY)
                    command.run()
                }
            }
        }

        //callback executor
        var callbackExecutor: Executor by Delegates.readWriteLazy { MainThreadExecutor() }

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

    fun download(path: String, param: Map<String, Any?>? = null): Request {
        val requestConvertible = build(Encoding()) {
            httpMethod = Method.GET
            baseUrlString = basePath
            urlString = path
            parameters = param
        }
        val request = requestConvertible.request
        request.requestType = Request.Type.DOWNLOAD
        return request
    }

    fun request(convertible: Fuel.RequestConvertible): Request {
        return convertible.request
    }

}
