package com.github.kittinunf.fuel.core.executors

import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSocketFactory

internal typealias RequestTransformer = (Request) -> Request
internal typealias ResponseTransformer = (Request, Response) -> Response

typealias InterruptCallback = ((Request) -> Unit)

interface RequestExecutor : Callable<Response>, RequestOptions {
    val request: Request

    var client: Client
    var socketFactory: SSLSocketFactory
    var hostnameVerifier: HostnameVerifier
    var executor: ExecutorService
    var callbackExecutor: Executor
    var requestTransformer: RequestTransformer
    var responseTransformer: ResponseTransformer
    val interruptCallback: InterruptCallback?

    var timeoutInMilliseconds: Int
    var timeoutReadInMilliseconds: Int
    var useHttpCaching: Boolean?
    var followRedirects: Boolean?
    val decodeContent: Boolean?

    fun <V> submit(callable: Callable<V>): Future<V>
}

interface RequestOptions {
    fun timeout(timeout: Int): Request
    fun timeoutRead(timeout: Int): Request
    fun caching(value: Boolean?): Request
    fun followRedirects(value: Boolean?): Request
    fun interrupt(callback: InterruptCallback): Request
}