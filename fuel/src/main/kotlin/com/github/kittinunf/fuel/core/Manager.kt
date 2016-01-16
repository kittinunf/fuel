package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.toolbox.HttpClient
import com.github.kittinunf.fuel.util.readWriteLazy
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.net.ssl.*

/**
 * Created by Kittinun Vantasin on 5/14/15.
 */

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
        } ?: unsecuredSocketFactory()
    }

    var hostnameVerifier: HostnameVerifier by readWriteLazy {
        unsecuredHostnameVerifier()
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

    //callback executor
    var callbackExecutor: Executor by readWriteLazy { createEnvironment().callbackExecutor }

    fun request(method: Method, path: String, param: List<Pair<String, Any?>>? = null): Request {
        val request = request(Encoding().apply {
            httpMethod = method
            baseUrlString = basePath
            urlString = path
            parameters = if (param == null) baseParams else baseParams + param
        })

        request.socketFactory = socketFactory
        request.hostnameVerifier = hostnameVerifier
        request.executor = executor
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

        request.socketFactory = socketFactory
        request.hostnameVerifier = hostnameVerifier
        request.executor = executor
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

        request.socketFactory = socketFactory
        request.hostnameVerifier = hostnameVerifier
        request.executor = executor
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

        request.socketFactory = socketFactory
        request.hostnameVerifier = hostnameVerifier
        request.executor = executor
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

        request.socketFactory = socketFactory
        request.hostnameVerifier = hostnameVerifier
        request.executor = executor
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

        request.socketFactory = socketFactory
        request.hostnameVerifier = hostnameVerifier
        request.executor = executor
        request.callbackExecutor = callbackExecutor
        return request
    }

    fun request(convertible: Fuel.RequestConvertible): Request {
        val request = convertible.request
        request.socketFactory = socketFactory
        request.hostnameVerifier = hostnameVerifier
        request.executor = executor
        request.callbackExecutor = callbackExecutor
        return request
    }

    private fun unsecuredSocketFactory(): SSLSocketFactory {
        val trustAllCerts = arrayOf(object : X509TrustManager {

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String) {
            }

            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String) {
            }

            override fun getAcceptedIssuers(): Array<out X509Certificate>? {
                return null
            }

        })

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        return sslContext.socketFactory
    }

    private fun unsecuredHostnameVerifier(): HostnameVerifier {
        return HostnameVerifier { hostname, session -> true }
    }

    companion object {
        //manager
        var instance by readWriteLazy { Manager() }

    }

}
