package com.github.kittinunf.fuel.core.interceptors

import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response

fun <T> loggingRequestInterceptor() =
        { next: (T) -> T ->
            { t: T ->
                println(t.toString())
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
        { request: Request, response: Response ->
            println(request.toString())
            println(response.toString())
            response
        }

