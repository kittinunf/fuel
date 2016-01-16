package com.github.kittinunf.fuel.core

public interface Client {
    fun executeRequest(request: Request): Response
}