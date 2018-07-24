package com.github.kittinunf.fuel.core.interceptors

import com.github.kittinunf.fuel.core.Encoding
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.isStatusRedirection
import java.net.URI
import java.net.URL
import javax.net.ssl.HttpsURLConnection

private val redirectStatusWithGets = listOf(HttpsURLConnection.HTTP_MOVED_PERM,
        HttpsURLConnection.HTTP_MOVED_TEMP,
        HttpsURLConnection.HTTP_SEE_OTHER)

class RedirectException : Exception("Redirection fail, not found URL to redirect to")

fun redirectResponseInterceptor(manager: FuelManager) =
        { next: (Request, Response) -> Response ->
            { request: Request, response: Response ->

                val newMethod = when (response.statusCode) {
                    in redirectStatusWithGets -> Method.GET
                    else -> {
                        request.method
                    }
                }

                if (response.isStatusRedirection) {
                    val redirectedUrl = response.headers["Location"] ?: response.headers["location"]

                    if (redirectedUrl?.isNotEmpty() == true) {
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
                        // there is no location detected, just passing along
                        next(request, response)
                    }
                } else {
                    next(request, response)
                }
            }
        }


