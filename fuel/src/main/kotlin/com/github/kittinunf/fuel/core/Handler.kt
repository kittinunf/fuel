package com.github.kittinunf.fuel.core

/**
 * Created by Kittinun Vantasin on 6/18/15.
 */

interface Handler<T> {
    fun success(request: Request, response: Response, value: T);
    fun failure(request: Request, response: Response, error: FuelError);
}
