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

fun redirectResponseInterceptor(manager: FuelManager) =
        { next: (Request, Response) -> Response ->
            { request: Request, response: Response ->

                if (response.isStatusRedirection && request.isAllowRedirects) {
                    val redirectedUrl = response.headers["Location"] ?: response.headers["location"]
                    val newMethod = when (response.statusCode) {
                        in redirectStatusWithGets -> Method.GET
                        else -> {
                            request.method
                        }
                    }

                    if (redirectedUrl?.isEmpty() == false) {
                        val redirectedUrlString = redirectedUrl.first()
                        val newUrl = if (URI(redirectedUrlString).isAbsolute) {
                            URL(redirectedUrlString)
                        } else {
                            URL(request.url, redirectedUrlString)
                        }
                        val newHeaders = request.headers.toMutableMap()

                        val encoding = Encoding(httpMethod = newMethod, urlString = newUrl.toString())

                        // check whether it is the same host or not
                        if (newUrl.host != request.url.host) {
                            newHeaders.remove("Authorization")
                        }

                        // redirect
                        next(request, manager.request(encoding).header(newHeaders).response().second)
                    } else {
                        // there is no location detected, just passing along
                        next(request, response)
                    }
                } else {
                    next(request, response)
                }
            }
        }
