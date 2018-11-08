package com.github.kittinunf.fuel.core

import com.github.kittinunf.result.Result

/**
 * Handler for [Response] transformations
 *
 * @see ResponseHandler for the variant with the request and response metadata
 * @see ResultHandler for the fold variant
 */
interface Handler<in T> {
    fun success(value: T)
    fun failure(error: FuelError)
}

/**
 * Handler for [Response] transformations, including metadata
 *
 * @see Handler for the variant without the request and response metadata
 * @see ResponseResultHandler for the fold variant
 *
 * @see ResponseOf
 */
interface ResponseHandler<in T> {
    fun success(request: Request, response: Response, value: T)
    fun failure(request: Request, response: Response, error: FuelError)
}

/**
 * Handler for [Response] transformations, wrapped in [Result]
 *
 * @see Handler for the unfolded variant
 * @see ResponseResultHandler for the variant including the request and response metadata
 */
typealias ResultHandler<T> = (Result<T, FuelError>) -> Unit

/**
 * Handler for [Response] transformations, wrapped in [Result], including metadata
 *
 * @see ResponseHandler for the unfolded variant
 * @see ResultHandler for the variant without the request and response metadata
 *
 * @see ResponseResultOf
 */
typealias ResponseResultHandler<T> = (Request, Response, Result<T, FuelError>) -> Unit
