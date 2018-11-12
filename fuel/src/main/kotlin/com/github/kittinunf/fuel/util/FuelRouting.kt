package com.github.kittinunf.fuel.util

import com.github.kittinunf.fuel.core.Encoding
import com.github.kittinunf.fuel.core.HeaderValues
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Parameters
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.RequestFactory

/**
 * Created by matteocrippa on 8/16/17.
 */
interface FuelRouting : RequestFactory.RequestConvertible {
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
    val params: Parameters?
    /**
     * Body to handle binary type of request (e.g. application/octet-stream )
     */
    val bytes: ByteArray?
    /**
     * Body to handle other type of request (e.g. application/json )
     */
    val body: String?
    /**
     * Headers for remote call.
     */
    val headers: Map<String, HeaderValues>?

    /**
     * @return a Request object.
     * Request call, it adheres to Fuel.RequestConvertible.
     */
    override val request: Request
        get() {
            // generate the encoder according provided parameters, headers, path, etc.
            val request = Encoding(
                    baseUrlString = basePath,
                    httpMethod = method,
                    urlString = path,
                    parameters = params
            ).request
            body?.let { request.body(it) } ?: bytes?.let { request.body(it) }

            // return the generated encoder with custom header injected
            return request.header(headers ?: emptyMap())
        }
}
