package com.github.kittinunf.fuel.reactor

import com.github.kittinunf.fuel.Fuel

fun main(args: Array<String>) {
    Fuel.get("https://icanhazdadjoke.com")
        .header("Accept" to "text/plain")
        .monoString()
        .subscribe(::println)
}
