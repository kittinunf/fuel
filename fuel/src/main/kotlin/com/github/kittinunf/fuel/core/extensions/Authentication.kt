package com.github.kittinunf.fuel.core.extensions

import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.util.encodeBase64ToString

fun Request.authenticate(username: String, password: String) = basicAuthentication(username, password)

fun Request.basicAuthentication(username: String, password: String): Request {
    val auth = "$username:$password"
    val encodedAuth = auth.encodeBase64ToString()
    this[Headers.AUTHORIZATION] = "Basic $encodedAuth"
    return request
}

fun Request.bearerAuthentication(bearerToken: String): Request {
    this[Headers.AUTHORIZATION] = "Bearer $bearerToken"
    return request
}