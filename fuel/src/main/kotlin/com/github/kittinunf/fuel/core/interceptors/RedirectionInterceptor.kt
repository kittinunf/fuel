package com.github.kittinunf.fuel.core.interceptors

import com.github.kittinunf.fuel.core.Encoding
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import java.net.URI
import java.net.URL
import javax.net.ssl.HttpsURLConnection

const val HTTP_PERMANENT_REDIRECT = 308

class RedirectException : Exception("Redirection fail, not found URL to redirect to")

fun redirectResponseInterceptor(manager: FuelManager) =
        { next: (Request, Response) -> Response ->
            { request: Request, response: Response ->

                val newMethod = when (response.statusCode) {
                    HttpsURLConnection.HTTP_MOVED_PERM,
                    HttpsURLConnection.HTTP_MOVED_TEMP,
                    HttpsURLConnection.HTTP_SEE_OTHER ->
                        Method.GET
                    else -> {
                        request.method
                    }
                }

                if (response.statusCode in HttpsURLConnection.HTTP_MULT_CHOICE..HTTP_PERMANENT_REDIRECT) {
                    val redirectedUrl = response.headers["Location"] ?: response.headers["location"]

                    if (redirectedUrl != null && redirectedUrl.isNotEmpty()) {
                        val newUrl = redirectedUrl.first()
                        val encoding = Encoding(httpMethod = newMethod,
                                urlString =
                                (if (URI(newUrl).isAbsolute) {
                                    URL(newUrl).toString()
                                } else {
                                    URL(request.url, newUrl)
                                }).toString())

                        // redirect
                        next(request, manager.request(encoding).response().second)
                    } else {
                        // error
                        val error = FuelError(
                                exception = RedirectException(),
                                errorData = response.data,
                                response = response
                        )
                        throw error
                    }
                } else {
                    next(request, response)
                }
            }
        }


