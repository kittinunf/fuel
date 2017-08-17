package com.github.kittinunf.fuel.util

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Encoding
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request

/**
 * Created by matteocrippa on 8/16/17.
 */
interface FuelRouting: Fuel.RequestConvertible {
    // contain the base path of the call
    val basePath: String
    // contain the method for the call
    val method: Method
    // contain the path for the call
    val path: String
    // contain the parameters for the call
    val params: List<Pair<String, Any?>>?
    // contain the headers for the call
    val headers: Map<String, String>?


    // request variable according Fuel.RequestConvertible interface
    override val request: Request
        get() {
            // generate the encoder according provided parameters, headers, path, etc.
            val encoder = Encoding().apply {
                this.baseUrlString = basePath
                this.httpMethod = method
                this.urlString = path
                this.parameters = params
                // FIXME: headers are missing
            }

            // return the generated encoder
            return encoder.request
        }
}