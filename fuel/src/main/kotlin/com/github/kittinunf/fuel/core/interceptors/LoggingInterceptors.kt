package com.github.kittinunf.fuel.core.interceptors

import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.extensions.cUrlString

fun <T> loggingRequestInterceptor() =
        { next: (T) -> T ->
            { t: T ->
                println(t)
                next(t)
            }
        }

fun cUrlLoggingRequestInterceptor() =
        { next: (Request) -> Request ->
            { r: Request ->
                println(r.cUrlString())
                next(r)
            }
        }

fun loggingResponseInterceptor(): (Request, Response) -> Response =
        { _: Request, response: Response ->
            println(response)
            response
        }
