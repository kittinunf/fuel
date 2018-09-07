package com.github.kittinunf.fuel.reactor

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.isSuccessful

fun main(args: Array<String>) {
    FuelManager.instance.basePath = "https://httpbin.org"

    Fuel.get("/status/404").monoOfResponse()
        .filter(Response::isSuccessful)
        .switchIfEmpty(Fuel.get("/status/200").monoOfResponse())
        .map(Response::statusCode)
        .subscribe(::println)
}
