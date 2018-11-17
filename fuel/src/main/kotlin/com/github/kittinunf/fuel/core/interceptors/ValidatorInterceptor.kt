package com.github.kittinunf.fuel.core.interceptors

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.HttpException
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response

internal class StatusCodeNotInRange(response: Response) :
    FuelError(HttpException(response.statusCode, response.responseMessage), response)

fun validatorResponseInterceptor(validRange: IntRange) = validatorResponseInterceptor(validRange.asIterable())
fun validatorResponseInterceptor(validCodes: Iterable<Int>) =
    { next: (Request, Response) -> Response ->
        { request: Request, response: Response ->
            if (validCodes.contains(response.statusCode))
                next(request, response)
            else
                throw StatusCodeNotInRange(response)
        }
    }
