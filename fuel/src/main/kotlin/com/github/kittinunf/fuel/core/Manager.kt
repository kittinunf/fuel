package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.toolbox.HttpClient
import com.github.kittinunf.fuel.util.readWriteLazy
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by Kittinun Vantasin on 5/14/15.
 */

public class Manager {

    public var client: Client by readWriteLazy { HttpClient() }
    public var basePath: String? = null

    public var baseHeaders: Map<String, String>? = null
    public var baseParams: List<Pair<String, Any?>> = emptyList()

    //background executor
    public var executor: ExecutorService by readWriteLazy {
        Executors.newCachedThreadPool { command ->
            Thread {
                Thread.currentThread().priority = Thread.NORM_PRIORITY
                command.run()
            }
        }
    }

    //callback executor
    public var callbackExecutor: Executor by readWriteLazy { createEnvironment().callbackExecutor }

    companion object {

        //manager
        var instance by readWriteLazy { Manager() }

    }

    fun request(method: Method, path: String, param: List<Pair<String, Any?>>? = null): Request {
        val request = request(Encoding().apply {
            httpMethod = method
            baseUrlString = basePath
            urlString = path
            parameters = if (param == null) baseParams else baseParams + param
        })

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
