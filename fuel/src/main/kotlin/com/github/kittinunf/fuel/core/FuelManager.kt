package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.interceptors.redirectResponseInterceptor
import com.github.kittinunf.fuel.core.interceptors.validatorResponseInterceptor
import com.github.kittinunf.fuel.core.requests.DownloadRequest
import com.github.kittinunf.fuel.core.requests.UploadRequest
import com.github.kittinunf.fuel.core.requests.download
import com.github.kittinunf.fuel.core.requests.upload
import com.github.kittinunf.fuel.toolbox.HttpClient
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
    var timeoutInMillisecond: Int = 15_000
    var timeoutReadInMillisecond: Int = timeoutInMillisecond
    var progressBufferSize: Int = DEFAULT_BUFFER_SIZE

    var baseHeaders: Map<String, String>? = null
    var baseParams: Parameters = emptyList()

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

    // background executionOptions
    var executorService: ExecutorService by readWriteLazy {
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

    // callback executionOptions
    var callbackExecutor: Executor by readWriteLazy { createEnvironment().callbackExecutor }

    fun request(method: Method, path: String, param: Parameters? = null): Request {
        val request = request(Encoding(
            httpMethod = method,
            urlString = path,
            baseUrlString = basePath,
            parameters = if (param == null) baseParams else baseParams + param
        ).request)
        return request(request)
    }

    fun request(method: Method, convertible: Fuel.PathStringConvertible, param: Parameters? = null): Request =
            request(method, convertible.path, param)

    fun download(path: String, param: Parameters? = null): DownloadRequest {
        val request = Encoding(
            httpMethod = Method.GET,
            urlString = path,
            baseUrlString = basePath,
            parameters = if (param == null) baseParams else baseParams + param
        ).request
        return request(request).download()
    }

    fun upload(path: String, method: Method = Method.POST, param: Parameters? = null): UploadRequest {
        val request = Encoding(
            httpMethod = method,
            urlString = path,
            baseUrlString = basePath,
            parameters = if (param == null) baseParams else baseParams + param
        ).request
        return request(request).upload()
    }

    fun request(convertible: Fuel.RequestConvertible): Request = request(convertible.request)

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

    private fun request(request: Request): Request {
        // Sets base headers ONLY if they are not set
        val unsetBaseHeaders = request.headers.keys.fold(Headers.from(baseHeaders.orEmpty())) {
            result, it -> result.remove(it); result
        }

        return request.header(unsetBaseHeaders).apply {
            executionOptions = RequestExecutionOptions(
                client = client,
                socketFactory = socketFactory,
                hostnameVerifier = hostnameVerifier,
                callbackExecutor = callbackExecutor,
                requestTransformer = requestInterceptors.foldRight({ r: Request -> r }) { f, acc -> f(acc) },
                responseTransformer = responseInterceptors.foldRight({ _: Request, res: Response -> res }) { f, acc -> f(acc) },
                executorService = executorService
            ).also { executor ->
                executor.timeoutInMillisecond = timeoutInMillisecond
                executor.timeoutReadInMillisecond = timeoutReadInMillisecond
            }
        }
    }

    companion object {
        // manager
        var instance by readWriteLazy { FuelManager() }
        val progressBufferSize: Int get() = instance.progressBufferSize
    }
}
