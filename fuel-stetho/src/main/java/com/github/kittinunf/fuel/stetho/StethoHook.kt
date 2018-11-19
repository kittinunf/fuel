package com.github.kittinunf.fuel.stetho

import com.facebook.stetho.urlconnection.ByteArrayRequestEntity
import com.facebook.stetho.urlconnection.StethoURLConnectionManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.toolbox.HttpClient
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection

class StethoHook(val friendlyName: String = "StethoFuelConnectionManager") : HttpClient.StethoHook {
    val stetho = StethoURLConnectionManager(friendlyName)

    override fun preConnect(connection: HttpURLConnection, request: Request) {
        stetho.preConnect(connection, ByteArrayRequestEntity(request.body.toByteArray()))
    }

    override fun interpretResponseStream(inputStream: InputStream): InputStream {
        return stetho.interpretResponseStream(inputStream)
    }

    override fun postConnect() {
        stetho.postConnect()
    }

    override fun httpExchangeFailed(exception: IOException) {
        stetho.httpExchangeFailed(exception)
    }
}