package com.github.kittinunf.fuel.core

import kotlin.properties.Delegates

/**
 * Created by Kittinun Vantasin on 5/18/15.
 */

class FuelError : Exception() {
    var exception: Exception by Delegates.notNull()
    var errorData = ByteArray(0)

    override fun toString(): String = "Exception : ${exception.message}"
}
