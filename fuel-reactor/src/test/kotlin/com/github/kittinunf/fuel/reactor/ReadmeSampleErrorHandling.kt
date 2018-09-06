package com.github.kittinunf.fuel.reactor

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.result.Result

data class Guest(val name: String)

object GuestMapper : ResponseDeserializable<Guest> {
    override fun deserialize(content: String) =
        jacksonObjectMapper().readValue<Guest>(content)
}

fun main(args: Array<String>) {
    Fuel.get("/guestName").monoOfResultObject(GuestMapper)
        .map(Result<Guest, FuelError>::get)
        .map { (name) -> "Welcome to the party, $name!" }
        .onErrorReturn("I'm sorry, your name is not on the list.")
        .subscribe(::println)
}
