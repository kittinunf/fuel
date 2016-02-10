package com.github.kittinunf.fuel.core

interface Client {
    fun executeRequest(request: Request): Response
}