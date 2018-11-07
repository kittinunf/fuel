package com.github.kittinunf.fuel.core.extensions

import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.util.encodeBase64ToString

class AuthenticatedRequest(private val wrapped: Request): Request by wrapped {
    override val request: AuthenticatedRequest = this

    /**
     * Adds basic authentication to the request
     *
     * @param username [String] the username
     * @param password [String] the password
     * @return [Request] the modified request
     */
    fun basic(username: String, password: String): Request {
        val auth = "$username:$password"
        val encodedAuth = auth.encodeBase64ToString()
        this[Headers.AUTHORIZATION] = "Basic $encodedAuth"
        return request
    }

    /**
     * Add bearer authentication to the request
     *
     * @param token [String] the bearer token
     * @return [Request] the modified request
     */
    fun bearer(token: String): Request {
        this[Headers.AUTHORIZATION] = "Bearer $token"
        return request
    }
}

fun Request.authentication() = AuthenticatedRequest(this)

@Deprecated("Use authentication() extension", replaceWith = ReplaceWith("authentication().basic(username, password)"))
fun Request.authenticate(username: String, password: String) = authentication().basic(username, password)
