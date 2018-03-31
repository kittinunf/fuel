package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.interceptors.redirectResponseInterceptor
import com.github.kittinunf.fuel.core.interceptors.validatorResponseInterceptor
import com.github.kittinunf.fuel.toolbox.HttpClient
import com.github.kittinunf.fuel.util.SameThreadExecutorService
import com.github.kittinunf.fuel.util.readWriteLazy
import java.net.Proxy
import java.security.KeyStore
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory

class FuelManager {
    var client: Client by readWriteLazy { HttpClient(proxy) }
    var proxy: Proxy? = null
    var basePath: String? = null
    var timeoutInMillisecond: Int = 15000
    var timeoutReadInMillisecond: Int = timeoutInMillisecond

    var baseHeaders: Map<String, String>? = null
    var baseParams: List<Pair<String, Any?>> = emptyList()

    var keystore: KeyStore? = null
    var socketFactory: SSLSocketFactory by readWriteLazy {
        keystore?.let {
            val trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustFactory.init(it)
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustFactory.trustManagers, null)
            sslContext.socketFactory
        } ?: HttpsURLConnection.getDefaultSSLSocketFactory()
    }

    var hostnameVerifier: HostnameVerifier by readWriteLazy {
        HttpsURLConnection.getDefaultHostnameVerifier()
    }

    //background executor
    var executor: ExecutorService by readWriteLazy {
        Executors.newCachedThreadPool { command ->
            Thread(command).also { thread ->
                thread.priority = Thread.NORM_PRIORITY
                thread.isDaemon = true
            }
        }
    }

    private val requestInterceptors: MutableList<((Request) -> Request) -> ((Request) -> Request)> =
            mutableListOf()
    private val responseInterceptors: MutableList<((Request, Response) -> Response) -> ((Request, Response) -> Response)> =
            mutableListOf(redirectResponseInterceptor(this), validatorResponseInterceptor(200..299))

    private fun createExecutor() = if (Fuel.testConfiguration.blocking) SameThreadExecutorService() else executor

    //callback executor
    var callbackExecutor: Executor by readWriteLazy { createEnvironment().callbackExecutor }

    fun request(method: Method, path: String, param: List<Pair<String, Any?>>? = null): Request {
        val request = request(Encoding(
                httpMethod = method,
                urlString = path,
                baseUrlString = basePath,
                parameters = if (param == null) baseParams else baseParams + param,
                timeoutInMillisecond = timeoutInMillisecond,
                timeoutReadInMillisecond = timeoutReadInMillisecond
        ).request)

        request.client = client
        request.headers += baseHeaders.orEmpty()
        request.socketFactory = socketFactory
        request.hostnameVerifier = hostnameVerifier
        request.executor = createExecutor()
        request.callbackExecutor = callbackExecutor
        request.requestInterceptor = requestInterceptors.foldRight({ r: Request -> r }) { f, acc -> f(acc) }
        request.responseInterceptor = responseInterceptors.foldRight({ _: Request, res: Response -> res }) { f, acc -> f(acc) }
        return request
    }

    fun request(method: Method, convertible: Fuel.PathStringConvertible, param: List<Pair<String, Any?>>? = null): Request =
            request(method, convertible.path, param)

    fun download(path: String, param: List<Pair<String, Any?>>? = null): Request {
        val request = Encoding(
                httpMethod = Method.GET,
                urlString = path,
                requestType = Request.Type.DOWNLOAD,
                baseUrlString = basePath,
                parameters = if (param == null) baseParams else baseParams + param,
                timeoutInMillisecond = timeoutInMillisecond,
                timeoutReadInMillisecond = timeoutReadInMillisecond
        ).request

        request.client = client
        request.headers += baseHeaders.orEmpty()
        request.socketFactory = socketFactory
        request.hostnameVerifier = hostnameVerifier
        request.executor = createExecutor()
        request.callbackExecutor = callbackExecutor
        request.requestInterceptor = requestInterceptors.foldRight({ r: Request -> r }) { f, acc -> f(acc) }
        request.responseInterceptor = responseInterceptors.foldRight({ _: Request, res: Response -> res }) { f, acc -> f(acc) }
        return request
    }

    fun upload(path: String, method: Method = Method.POST, param: List<Pair<String, Any?>>? = null): Request {
        val request = Encoding(
                httpMethod = method,
                urlString = path,
                requestType = Request.Type.UPLOAD,
                baseUrlString = basePath,
                parameters = if (param == null) baseParams else baseParams + param,
                timeoutInMillisecond = timeoutInMillisecond,
                timeoutReadInMillisecond = timeoutReadInMillisecond
        ).request

        request.client = client
        request.headers += baseHeaders.orEmpty()
        request.socketFactory = socketFactory
        request.hostnameVerifier = hostnameVerifier
        request.executor = createExecutor()
        request.callbackExecutor = callbackExecutor
        request.requestInterceptor = requestInterceptors.foldRight({ r: Request -> r }) { f, acc -> f(acc) }
        request.responseInterceptor = responseInterceptors.foldRight({ _: Request, res: Response -> res }) { f, acc -> f(acc) }
        return request
    }

    fun request(convertible: Fuel.RequestConvertible): Request {
        val request = convertible.request
        request.client = client
        request.headers += baseHeaders.orEmpty()
        request.socketFactory = socketFactory
        request.hostnameVerifier = hostnameVerifier
        request.executor = createExecutor()
        request.callbackExecutor = callbackExecutor
        request.requestInterceptor = requestInterceptors.foldRight({ r: Request -> r }) { f, acc -> f(acc) }
        request.responseInterceptor = responseInterceptors.foldRight({ _: Request, res: Response -> res }) { f, acc -> f(acc) }
        return request
    }

    fun addRequestInterceptor(interceptor: ((Request) -> Request) -> ((Request) -> Request)) {
        requestInterceptors += interceptor
    }

    fun addResponseInterceptor(interceptor: ((Request, Response) -> Response) -> ((Request, Response) -> Response)) {
        responseInterceptors += interceptor
    }

    fun removeRequestInterceptor(interceptor: ((Request) -> Request) -> ((Request) -> Request)) {
        requestInterceptors -= interceptor
    }

    fun removeResponseInterceptor(interceptor: ((Request, Response) -> Response) -> ((Request, Response) -> Response)) {
        responseInterceptors -= interceptor
    }

    fun removeAllRequestInterceptors() {
        requestInterceptors.clear()
    }

    fun removeAllResponseInterceptors() {
        responseInterceptors.clear()
    }

    companion object {
        //manager
        var instance by readWriteLazy { FuelManager() }

    }

}
