package com.github.kittinunf.fuel.core.interceptors

import com.github.kittinunf.fuel.core.*
import java.net.MalformedURLException
import java.net.URI
import java.net.URL
import javax.net.ssl.HttpsURLConnection.HTTP_MULT_CHOICE //300
import javax.net.ssl.HttpsURLConnection.HTTP_MOVED_PERM //301
import javax.net.ssl.HttpsURLConnection.HTTP_MOVED_TEMP//302
import javax.net.ssl.HttpsURLConnection.HTTP_SEE_OTHER//303

class RedirectException : Exception("Redirection fail, not found URL to redirect to")

fun redirectResponseInterceptor(manager: FuelManager) =
        { next: (Request, Response) -> Response ->
            { request: Request, response: Response ->
                when (response.statusCode) {
                    HTTP_MULT_CHOICE, //300
                    HTTP_MOVED_PERM, //301
                    HTTP_MOVED_TEMP, //302
                    HTTP_SEE_OTHER, //303
                    307,
                    308 -> {
                        val redirectedUrl = response.headers["Location"] ?: response.headers["location"]
                        if (redirectedUrl != null && !redirectedUrl.isEmpty()) {
                            val encoding = Encoding(
                                    httpMethod = request.method,
                                    urlString =
                                    if (URI(redirectedUrl[0]).isAbsolute) {
                                        URL(redirectedUrl[0]).toString()
                                    } else {
                                        // Maybe its a relative url. Use the original for context.
                                        URL(request.url, redirectedUrl[0]).toString()
                                    }
                            )
                            next(request, manager.request(encoding).response().second)
                        } else {
                            //error
                            val error = FuelError(
                                exception = RedirectException(),
                                errorData = response.data,
                                response = response
                            )
                            throw error
                        }
                    }
                    else -> {
                        next(request, response)
                    }
                }
            }
        }
