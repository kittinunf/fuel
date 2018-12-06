package com.github.kittinunf.fuel.core.interceptors

import com.github.kittinunf.fuel.core.FoldableRequestInterceptor
import com.github.kittinunf.fuel.core.FoldableResponseInterceptor
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.RequestTransformer
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.ResponseTransformer
import com.github.kittinunf.fuel.core.extensions.cUrlString

object LogRequestInterceptor : FoldableRequestInterceptor {
    override fun invoke(next: RequestTransformer): RequestTransformer {
        return { request ->
            println(request)
            next(request)
        }
    }
}

object LogRequestAsCurlInterceptor : FoldableRequestInterceptor {
    override fun invoke(next: RequestTransformer): RequestTransformer {
        return { request ->
            println(request.cUrlString())
            next(request)
        }
    }
}

object LogResponseInterceptor : FoldableResponseInterceptor {
    override fun invoke(next: ResponseTransformer): ResponseTransformer {
        return { request, response ->
            println(response.toString())
            next(request, response)
        }
    }
}

@Deprecated("Use LogRequestInterceptor", replaceWith = ReplaceWith("LogRequestInterceptor"))
fun <T> loggingRequestInterceptor() =
        { next: (T) -> T ->
            { t: T ->
                println(t)
                next(t)
            }
        }

@Deprecated("Use LogRequestAsCurlInterceptor", replaceWith = ReplaceWith("LogRequestAsCurlInterceptor"))
fun cUrlLoggingRequestInterceptor() =
        { next: (Request) -> Request ->
            { r: Request ->
                println(r.cUrlString())
                next(r)
            }
        }

@Deprecated("Use LogRequestAsCurlInterceptor (remove braces)", replaceWith = ReplaceWith("LogResponseInterceptor"))
fun loggingResponseInterceptor(): (Request, Response) -> Response =
        { _: Request, response: Response ->
            println(response)
            response
        }
