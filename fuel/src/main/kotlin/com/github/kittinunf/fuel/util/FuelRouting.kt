package com.github.kittinunf.fuel.util

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Encoding
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request

/**
 * Created by matteocrippa on 8/16/17.
 */
interface FuelRouting: Fuel.RequestConvertible {
    /**
     * Base path handler for the remote call.
     */
    val basePath: String
    /**
     * Method handler for the remote requests.
     */
    val method: Method
    /**
     * Path handler for the request.
     */
    val path: String
    /**
     * Parameters for the remote call.
     * It uses a pair with String, Any.
     */
    val params: List<Pair<String, Any?>>?
    /**
     * Headers for remote call.
     */
    val headers: Map<String, String>?


    /**
     * @return a Request object.
     * Request call, it adheres to Fuel.RequestConvertible.
     */
    override val request: Request
        get() {
            // generate the encoder according provided parameters, headers, path, etc.
            val encoder = Encoding().apply {
                this.baseUrlString = basePath
                this.httpMethod = method
                this.urlString = path
                this.parameters = params
            }

            // return the generated encoder with header injected
            return encoder.request.header(headers)
        }
}