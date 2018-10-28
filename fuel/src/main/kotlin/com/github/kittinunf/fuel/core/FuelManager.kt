package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.executors.RequestTransformer
import com.github.kittinunf.fuel.core.executors.ResponseTransformer
import com.github.kittinunf.fuel.core.interceptors.redirectResponseInterceptor
import com.github.kittinunf.fuel.core.interceptors.validatorResponseInterceptor
import com.github.kittinunf.fuel.core.requests.DownloadRequest
import com.github.kittinunf.fuel.core.requests.GenericRequestExecutor
import com.github.kittinunf.fuel.core.requests.MultipartRequest
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

typealias RequestInterceptor = (RequestTransformer) -> RequestTransformer
typealias ResponseInterceptor = (ResponseTransformer) -> ResponseTransformer

class FuelManager {
    var client: Client by readWriteLazy { HttpClient(proxy) }
    var proxy: Proxy? = null
    var basePath: String? = null
    var timeoutInMilliseconds: Int = 15000
    var timeoutReadInMilliseconds: Int = timeoutInMilliseconds

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

    // background executor
    var executor: ExecutorService by readWriteLazy {
        Executors.newCachedThreadPool { command ->
            Thread(command).also { thread ->
                thread.priority = Thread.NORM_PRIORITY
                thread.isDaemon = true
            }
        }
    }

    private val requestInterceptors: MutableList<RequestInterceptor> = mutableListOf()
    private val responseInterceptors: MutableList<ResponseInterceptor> = mutableListOf(
        redirectResponseInterceptor(this),
        validatorResponseInterceptor(200..299)
    )

    private fun createExecutor() = if (Fuel.testConfiguration.blocking) SameThreadExecutorService() else executor

    // callback executor
    var callbackExecutor: Executor by readWriteLazy { createEnvironment().callbackExecutor }

    fun request(method: Method, path: String, param: Parameters? = null): Request {
        val request = Encoding(
            httpMethod = method,
            urlString = path,
            baseUrlString = basePath,
            parameters = if (param == null) baseParams else baseParams + param
        ).request
        return applyManager(request)
    }

    fun request(method: Method, convertible: Fuel.PathStringConvertible, param: Parameters? = null): Request =
            request(method, convertible.path, param)

    fun request(convertible: Fuel.RequestConvertible) = applyManager(convertible.request)

    fun download(path: String, param: List<Pair<String, Any?>>? = null): DownloadRequest {
        val request = Encoding(
            httpMethod = Method.GET,
            urlString = path,
            baseUrlString = basePath,
            parameters = if (param == null) baseParams else baseParams + param
        ).request
        return applyManager(request).download()
    }

    fun upload(path: String, method: Method = Method.POST, param: Parameters? = null): MultipartRequest {
        val request = Encoding(
            httpMethod = method,
            urlString = path,
            baseUrlString = basePath,
            parameters = if (param == null) baseParams else baseParams + param
        ).request
        return applyManager(request).multipart()
    }

    fun addRequestInterceptor(interceptor: RequestInterceptor) {
        requestInterceptors += interceptor
    }

    fun addResponseInterceptor(interceptor: ResponseInterceptor) {
        responseInterceptors += interceptor
    }

    fun removeRequestInterceptor(interceptor: RequestInterceptor) {
        requestInterceptors -= interceptor
    }

    fun removeResponseInterceptor(interceptor: ResponseInterceptor) {
        responseInterceptors -= interceptor
    }

    fun removeAllRequestInterceptors() {
        requestInterceptors.clear()
    }

    fun removeAllResponseInterceptors() {
        responseInterceptors.clear()
    }

    private fun applyManager(request: Request): Request {
        // Sets base headers ONLY if they are not set
        val unsetBaseHeaders = request.headers.keys.fold(Headers.from(baseHeaders.orEmpty())) {
            result, it -> result.remove(it); result
        }

        request.executor = GenericRequestExecutor(
            request,
            client = client,
            socketFactory = socketFactory,
            hostnameVerifier = hostnameVerifier,
            executor = createExecutor(),
            callbackExecutor = callbackExecutor,
            requestTransformer = requestInterceptors.foldRight({ r: Request -> r }) { f, acc -> f(acc) },
            responseTransformer = responseInterceptors.foldRight({ _: Request, res: Response -> res }) { f, acc -> f(acc) },
            timeoutInMilliseconds = timeoutInMilliseconds,
            timeoutReadInMilliseconds = timeoutReadInMilliseconds
        )

        return request.header(unsetBaseHeaders)
    }

    companion object {
        // manager
        var instance by readWriteLazy { FuelManager() }
    }
}
