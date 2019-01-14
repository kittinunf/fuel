package com.github.kittinunf.fuel.core

import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection

interface Client {
    fun executeRequest(request: Request): Response
    suspend fun awaitRequest(request: Request): Response = executeRequest(request)

    interface Hook {
        fun preConnect(connection: HttpURLConnection, request: Request)
        fun interpretResponseStream(inputStream: InputStream): InputStream
        fun postConnect()
        fun httpExchangeFailed(exception: IOException)
    }
}