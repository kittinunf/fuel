package com.github.kittinunf.fuel.core.interceptors

import com.github.kittinunf.fuel.core.*
import java.net.MalformedURLException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class RedirectException : Exception("Redirection fail, not found URL to redirect to")

fun redirectResponseInterceptor(manager: FuelManager) =
        { next: (Request, Response) -> Response ->
            { request: Request, response: Response ->
                if (response.statusCode == HttpsURLConnection.HTTP_MOVED_PERM ||
                        response.statusCode == HttpsURLConnection.HTTP_MOVED_TEMP ||
                        response.statusCode == 307   // 307 TEMPORARY REDIRECT - https://httpstatuses.com/307
                        ) {
                    val redirectedUrl = response.headers["Location"]
                    if (redirectedUrl != null && !redirectedUrl.isEmpty()) {
                        val encoding = Encoding(
                                httpMethod = request.method,
                                urlString = try {
                                    URL(redirectedUrl[0]).toString()
                                } catch (e: MalformedURLException) {
                                    // Maybe its a relative url. Use the original for context.
                                    URL(request.url, redirectedUrl[0]).toString()
                                }
                        )
                        manager.request(encoding).response().second
                    } else {
                        //error
                        val error = FuelError(RedirectException(), response.data, response)
                        throw error
                    }
                } else {
                    next(request, response)
                }
            }
        }


