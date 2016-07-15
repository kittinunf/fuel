package com.github.kittinunf.fuel.core.interceptors

import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response

fun <T> loggingInterceptor() =
        { next: (T) -> T ->
            { t: T ->
                println(t.toString())
                next(t)
            }
        }

fun loggingResponseInterceptor() =
        { next: (Request, Response) -> Response ->
            { request: Request, response: Response ->
                println(response.toString())
                next(request, response)
            }
        }

fun cUrlLoggingRequestInterceptor() =
        { next: (Request) -> Request ->
            { r: Request ->
                println(r.cUrlString())
                next(r)
            }
        }
