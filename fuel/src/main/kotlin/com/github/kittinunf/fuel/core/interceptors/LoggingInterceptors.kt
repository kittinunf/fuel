package com.github.kittinunf.fuel.core.interceptors

import com.github.kittinunf.fuel.core.Request

fun <T> loggingInterceptor() =
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
