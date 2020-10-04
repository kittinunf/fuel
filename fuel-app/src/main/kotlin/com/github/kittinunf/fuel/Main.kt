package com.github.kittinunf.fuel

fun main(args: Array<String>) {
    println("Hello Fuel!")

    val (_, _, result) = "https://httpbin.org/get".httpGet().responseString()
    println(result)
}
