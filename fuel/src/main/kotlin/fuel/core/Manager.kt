package fuel.core

import fuel.Fuel
import fuel.toolbox.HttpClient
import fuel.util.AndroidMainThreadExecutor
import fuel.util.build
import fuel.util.readWriteLazy
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.properties.Delegates

/**
 * Created by Kittinun Vantasin on 5/14/15.
 */

public class Manager {

    public var client: Client by Delegates.notNull()
    public var basePath: String? = null

    public var baseHeaders: Map<String, String>? = null
    public var baseParams: Map<String, Any?> = mapOf()

    //background executor
    public var executor: ExecutorService by Delegates.readWriteLazy {
        Executors.newCachedThreadPool { command ->
            Thread {
                Thread.currentThread().setPriority(Thread.NORM_PRIORITY)
                command.run()
            }
        }
    }

    //callback executor
    public var callbackExecutor: Executor by Delegates.readWriteLazy { AndroidMainThreadExecutor() }

    companion object Singleton {

        //manager
        var instance by Delegates.readWriteLazy {
            build(Manager()) {
                this.client = HttpClient()
            }
        }

    }

    fun request(method: Method, path: String, param: Map<String, Any?>? = null): Request {
        val request = request(build(Encoding()) {
            httpMethod = method
            baseUrlString = basePath
            urlString = path
            parameters = if (param == null) baseParams else param + baseParams
        })

        request.executor = executor
        request.callbackExecutor = callbackExecutor
        return request
    }

    fun request(method: Method, convertible: Fuel.PathStringConvertible, param: Map<String, Any?>? = null): Request {
        val request = request(build(Encoding()) {
            httpMethod = method
            baseUrlString = basePath
            urlString = convertible.path
            parameters = if (param == null) baseParams else param + baseParams
        })

        request.executor = executor
        request.callbackExecutor = callbackExecutor
        return request
    }

    fun download(path: String, param: Map<String, Any?>? = null): Request {
        val request = build(Encoding()) {
            httpMethod = Method.GET
            baseUrlString = basePath
            urlString = path
            parameters = if (param == null) baseParams else param + baseParams
            requestType = Request.Type.DOWNLOAD
        }.request

        request.executor = executor
        request.callbackExecutor = callbackExecutor
        return request
    }

    fun download(convertible: Fuel.PathStringConvertible, param: Map<String, Any?>? = null): Request {
        val request = build(Encoding()) {
            httpMethod = Method.GET
            baseUrlString = basePath
            urlString = convertible.path
            parameters = if (param == null) baseParams else param + baseParams
            requestType = Request.Type.DOWNLOAD
        }.request

        request.executor = executor
        request.callbackExecutor = callbackExecutor
        return request
    }

    fun upload(path: String, param: Map<String, Any?>? = null): Request {
        val request = build(Encoding()) {
            httpMethod = Method.POST
            baseUrlString = basePath
            urlString = path
            parameters = if (param == null) baseParams else param + baseParams
            requestType = Request.Type.UPLOAD
        }.request

        request.executor = executor
        request.callbackExecutor = callbackExecutor
        return request
    }

    fun upload(convertible: Fuel.PathStringConvertible, param: Map<String, Any?>? = null): Request {
        val request = build(Encoding()) {
            httpMethod = Method.POST
            baseUrlString = basePath
            urlString = convertible.path
            parameters = if (param == null) baseParams else param + baseParams
            requestType = Request.Type.UPLOAD
        }.request

        request.executor = executor
        request.callbackExecutor = callbackExecutor
        return request
    }

    fun request(convertible: Fuel.RequestConvertible): Request {
        val request = convertible.request

        request.executor = executor
        request.callbackExecutor = callbackExecutor
        return request
    }

}
