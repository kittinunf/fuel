package com.github.kittinunf.fuel.core

import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection

interface Client {
    fun executeRequest(request: Request): Response
    suspend fun awaitRequest(request: Request): Response = executeRequest(request)

    interface Hook {
        fun preConnect(connection: HttpURLConnection, request: Request)
        fun postConnect(request: Request)
        fun interpretResponseStream(request: Request, inputStream: InputStream): InputStream
        fun httpExchangeFailed(request: Request, exception: IOException)
    }
}
