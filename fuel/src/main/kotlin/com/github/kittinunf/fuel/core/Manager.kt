package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.toolbox.HttpClient
import com.github.kittinunf.fuel.util.SameThreadExecutorService
import com.github.kittinunf.fuel.util.readWriteLazy
import java.security.KeyStore
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.net.ssl.*

class Manager {
    var client: Client by readWriteLazy { HttpClient() }
    var basePath: String? = null

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
            Thread {
                Thread.currentThread().priority = Thread.NORM_PRIORITY
                command.run()
            }
        }
    }

    fun createExecutor() = if (Fuel.testConfiguration.blocking) SameThreadExecutorService() else executor

    //callback executor
    var callbackExecutor: Executor by readWriteLazy { createEnvironment().callbackExecutor }

    fun request(method: Method, path: String, param: List<Pair<String, Any?>>? = null): Request {
        val request = request(Encoding().apply {
            httpMethod = method
            baseUrlString = basePath
            urlString = path
            parameters = if (param == null) baseParams else baseParams + param
        })

        request.httpHeaders = if (baseHeaders != null) HashMap(baseHeaders) else hashMapOf()
        request.socketFactory = socketFactory
        request.hostnameVerifier = hostnameVerifier
        request.executor = createExecutor()
        request.callbackExecutor = callbackExecutor
        return request
    }

    fun request(method: Method, convertible: Fuel.PathStringConvertible, param: List<Pair<String, Any?>>? = null): Request {
        val request = request(Encoding().apply {
            httpMethod = method
            baseUrlString = basePath
            urlString = convertible.path
            parameters = if (param == null) baseParams else baseParams + param
        })

        request.httpHeaders = if (baseHeaders != null) HashMap(baseHeaders) else hashMapOf()
        request.socketFactory = socketFactory
        request.hostnameVerifier = hostnameVerifier
        request.executor = createExecutor()
        request.callbackExecutor = callbackExecutor
        return request
    }

    fun download(path: String, param: List<Pair<String, Any?>>? = null): Request {
        val request = Encoding().apply {
            httpMethod = Method.GET
            baseUrlString = basePath
            urlString = path
            parameters = if (param == null) baseParams else baseParams + param
            requestType = Request.Type.DOWNLOAD
        }.request

        request.httpHeaders = if (baseHeaders != null) HashMap(baseHeaders) else hashMapOf()
        request.socketFactory = socketFactory
        request.hostnameVerifier = hostnameVerifier
        request.executor = createExecutor()
        request.callbackExecutor = callbackExecutor
        return request
    }

    fun download(convertible: Fuel.PathStringConvertible, param: List<Pair<String, Any?>>? = null): Request {
        val request = Encoding().apply {
            httpMethod = Method.GET
            baseUrlString = basePath
            urlString = convertible.path
            parameters = if (param == null) baseParams else baseParams + param
            requestType = Request.Type.DOWNLOAD
        }.request

        request.httpHeaders = if (baseHeaders != null) HashMap(baseHeaders) else hashMapOf()
        request.socketFactory = socketFactory
        request.hostnameVerifier = hostnameVerifier
        request.executor = createExecutor()
        request.callbackExecutor = callbackExecutor
        return request
    }

    fun upload(path: String, method: Method = Method.POST, param: List<Pair<String, Any?>>? = null): Request {
        val request = Encoding().apply {
            httpMethod = method
            baseUrlString = basePath
            urlString = path
            parameters = if (param == null) baseParams else baseParams + param
            requestType = Request.Type.UPLOAD
        }.request

        request.httpHeaders = if (baseHeaders != null) HashMap(baseHeaders) else hashMapOf()
        request.socketFactory = socketFactory
        request.hostnameVerifier = hostnameVerifier
        request.executor = createExecutor()
        request.callbackExecutor = callbackExecutor
        return request
    }

    fun upload(convertible: Fuel.PathStringConvertible, method: Method = Method.POST, param: List<Pair<String, Any?>>? = null): Request {
        val request = Encoding().apply {
            httpMethod = method
            baseUrlString = basePath
            urlString = convertible.path
            parameters = if (param == null) baseParams else baseParams + param
            requestType = Request.Type.UPLOAD
        }.request

        request.httpHeaders = if (baseHeaders != null) HashMap(baseHeaders) else hashMapOf()
        request.socketFactory = socketFactory
        request.hostnameVerifier = hostnameVerifier
        request.executor = createExecutor()
        request.callbackExecutor = callbackExecutor
        return request
    }

    fun request(convertible: Fuel.RequestConvertible): Request {
        val request = convertible.request
        request.httpHeaders = if (baseHeaders != null) HashMap(baseHeaders) else hashMapOf()
        request.socketFactory = socketFactory
        request.hostnameVerifier = hostnameVerifier
        request.executor = createExecutor()
        request.callbackExecutor = callbackExecutor
        return request
    }

    companion object {
        //manager
        var instance by readWriteLazy { Manager() }

    }

}
