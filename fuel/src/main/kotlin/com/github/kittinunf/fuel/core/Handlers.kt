package com.github.kittinunf.fuel.core

import com.github.kittinunf.result.Result

interface Handler<in T> {
    fun success(value: T)
    fun failure(error: FuelError)
}

interface ResponseHandler<in T> {
    fun success(request: Request, response: Response, value: T)
    fun failure(request: Request, response: Response, error: FuelError)
}

typealias ResultHandler<T> = (Result<T, FuelError>) -> Unit
typealias ResponseResultHandler<T> = (Request, Response, Result<T, FuelError>) -> Unit
