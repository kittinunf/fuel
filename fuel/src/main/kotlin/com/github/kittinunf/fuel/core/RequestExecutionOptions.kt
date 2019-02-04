package com.github.kittinunf.fuel.core

import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSocketFactory

typealias RequestTransformer = (Request) -> Request
typealias ResponseTransformer = (Request, Response) -> Response

typealias InterruptCallback = (Request) -> Unit

typealias ResponseValidator = (Response) -> Boolean

data class RequestExecutionOptions(
    val client: Client,
    val socketFactory: SSLSocketFactory? = null,
    val hostnameVerifier: HostnameVerifier? = null,
    val executorService: ExecutorService,
    val callbackExecutor: Executor,
    val requestTransformer: RequestTransformer,
    var responseTransformer: ResponseTransformer
) {
    val requestProgress: Progress = Progress()
    val responseProgress: Progress = Progress()
    var timeoutInMillisecond: Int = 15_000
    var timeoutReadInMillisecond: Int = 15_000
    var decodeContent: Boolean? = null
    var allowRedirects: Boolean? = null
    var useHttpCache: Boolean? = null
    var interruptCallbacks: MutableCollection<InterruptCallback> = mutableListOf()
    var forceMethods: Boolean = false
    var responseValidator: ResponseValidator = { response ->
        !(response.isServerError || response.isClientError)
    }

    /**
     * Executes a callback [f] onto the [Executor]
     *
     * @note this can be used to handle callbacks on a different Thread than the network request is made
     */
    fun callback(f: () -> Unit) = callbackExecutor.execute(f)

    /**
     * Submits the task to the [ExecutorService]
     *
     * @param task [Callable] the execution of [Request] that yields a [Response]
     * @return [Future] the future that resolves to a [Response]
     */
    fun submit(task: Callable<Response>): Future<Response> = executorService.submit(task)

    val interruptCallback: InterruptCallback = { request -> interruptCallbacks.forEach { it(request) } }

    /**
     * Append a response transformer
     */
    operator fun plusAssign(next: ResponseTransformer) {
        val previous = responseTransformer
        responseTransformer = { request, response -> next(request, previous(request, response)) }
    }
}
