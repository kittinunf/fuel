package com.github.kittinunf.fuel.toolbox.extensions

import com.github.kittinunf.fuel.core.Method
import java.net.HttpURLConnection

/**
 * Uses public setter "setRequestMethod" if method is support by HttpURLConnection
 * When method is PATCH, use reflection to set private field "method" on the HttpURLConnection instance
 * If reflection method fails, use "coerceMethod" and set appropriate "X-HTTP-Method-Override" header
 */
fun HttpURLConnection.forceMethod(method: Method) {
    when (method) {
        Method.PATCH -> {
            try {
                // If instance has private field "delegate" (HttpURLConnection), make the field accessible
                // and invoke "forceMethod" on that instance of HttpURLConnection
                (this.javaClass.getDeclaredField("delegate").apply {
                    this.isAccessible = true
                }.get(this) as HttpURLConnection?)?.forceMethod(method)
            } catch (ex: NoSuchFieldException) {
                // ignore
            }
            (arrayOf(this.javaClass.superclass, this.javaClass)).forEach {
                try {
                    it.getDeclaredField("method").apply {
                        this.isAccessible = true
                    }.set(this, method.value)
                } catch (ex: NoSuchFieldException) {
                    // ignore
                }
            }
        }
        else -> this.requestMethod = method.value
    }
}