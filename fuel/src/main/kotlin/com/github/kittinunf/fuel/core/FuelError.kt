package com.github.kittinunf.fuel.core

class FuelError(val exception: Exception, val errorData: ByteArray = ByteArray(0), val response: Response = Response.error())
    : Exception(exception) {
    init {
        stackTrace = exception.stackTrace
    }
    override fun toString(): String = "${exception.javaClass.canonicalName}: ${exception.message ?: "<no message>"}"
}
