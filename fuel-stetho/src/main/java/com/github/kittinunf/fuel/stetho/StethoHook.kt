package com.github.kittinunf.fuel.stetho

import com.facebook.stetho.urlconnection.ByteArrayRequestEntity
import com.facebook.stetho.urlconnection.StethoURLConnectionManager
import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.Request
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class StethoHook(val friendlyName: String = "StethoFuelConnectionManager") : Client.Hook {

    val stethoCache = ConcurrentHashMap<UUID, StethoURLConnectionManager>()

    override fun preConnect(connection: HttpURLConnection, request: Request) {
        // attach UUID tag for the request
        request.tag(UUID.randomUUID())

        val stetho = stethoCache.getOrPut(request.getTag(UUID::class)!!) {
            StethoURLConnectionManager(friendlyName)
        }
        stetho.preConnect(connection, ByteArrayRequestEntity(request.body.toByteArray()))
    }

    override fun postConnect(request: Request) {
        stethoCache[request.getTag(UUID::class)]?.postConnect()
    }

    // means the connection ended with success, allow stetho to intercept response, remove it from the cache
    override fun interpretResponseStream(request: Request, inputStream: InputStream?): InputStream? {
        val stetho = stethoCache.remove(request.getTag(UUID::class))
        return stetho?.interpretResponseStream(inputStream) ?: inputStream
    }

    // means the connection ended with failure, allow stetho to intercept failure response, remove it from the cache
    override fun httpExchangeFailed(request: Request, exception: IOException) {
        val stetho = stethoCache.remove(request.getTag(UUID::class))
        stetho?.httpExchangeFailed(exception)
    }
}
