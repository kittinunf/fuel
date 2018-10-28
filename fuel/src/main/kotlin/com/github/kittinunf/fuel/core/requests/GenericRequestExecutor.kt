package com.github.kittinunf.fuel.core.requests

import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.executors.InterruptCallback
import com.github.kittinunf.fuel.core.executors.RequestExecutor
import com.github.kittinunf.fuel.core.executors.RequestOptions
import com.github.kittinunf.fuel.core.executors.RequestTransformer
import com.github.kittinunf.fuel.core.executors.ResponseTransformer
import java.io.InterruptedIOException
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSocketFactory

data class GenericRequestExecutor(
    override val request: Request,
    override var client: Client,
    override var socketFactory: SSLSocketFactory,
    override var hostnameVerifier: HostnameVerifier,
    override var executor: ExecutorService,
    override var callbackExecutor: Executor,
    override var requestTransformer: RequestTransformer,
    override var responseTransformer: ResponseTransformer,
    override var interruptCallback: InterruptCallback? = null,

    override var timeoutInMilliseconds: Int = 15_000,
    override var timeoutReadInMilliseconds: Int = timeoutInMilliseconds,
    override var useHttpCaching: Boolean? = null,
    override var followRedirects: Boolean? = null,
    override var decodeContent: Boolean? = null
) : RequestExecutor, RequestOptions {

    override fun timeout(timeout: Int): Request {
        timeoutInMilliseconds = timeout
        return request
    }

    override fun timeoutRead(timeout: Int): Request {
        timeoutReadInMilliseconds = timeout
        return request
    }

    override fun caching(value: Boolean?): Request {
        useHttpCaching = value
        return request
    }

    override fun followRedirects(value: Boolean?): Request {
        followRedirects = value
        return request
    }

    override fun call(): Response = try {
        val modifiedRequest = requestTransformer.invoke(request)
        responseTransformer.invoke(modifiedRequest, client.executeRequest(modifiedRequest))
    } catch (error: FuelError) {
        if (error.exception as? InterruptedIOException != null) {
            interruptCallback?.invoke(request)
        }
        throw error
    } catch (exception: Exception) {
        throw FuelError(exception)
    }

    override fun <V> submit(callable: Callable<V>) = executor.submit(callable) as Future<V>

    override fun interrupt(callback: InterruptCallback): Request {
        interruptCallback = callback
        return request
    }
}