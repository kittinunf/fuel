package com.github.kittinunf.fuel.core

import com.github.kittinunf.result.Result

interface Client {
    fun executeRequest(request: Request): Response
    suspend fun awaitRequest(request: Request): Response
    // suspend fun awaitResult(): Result<Response, FuelError>
}