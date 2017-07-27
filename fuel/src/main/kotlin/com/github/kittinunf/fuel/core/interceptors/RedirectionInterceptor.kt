package com.github.kittinunf.fuel.core.interceptors

import com.github.kittinunf.fuel.core.*
import java.net.MalformedURLException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class RedirectException : Exception("Redirection fail, not found URL to redirect to")

fun redirectResponseInterceptor(manager: FuelManager) =
        { next: (Request, Response) -> Response ->
            { request: Request, response: Response ->
                if (response.httpStatusCode == HttpsURLConnection.HTTP_MOVED_PERM ||
                        response.httpStatusCode == HttpsURLConnection.HTTP_MOVED_TEMP ||
                        response.httpStatusCode == 307   // 307 TEMPORARY REDIRECT - https://httpstatuses.com/307
                   ) {
                    val redirectedUrl = response.httpResponseHeaders["Location"]
                    if (redirectedUrl != null && !redirectedUrl.isEmpty()) {
                        val encoding = Encoding().apply {
                            httpMethod = request.httpMethod
                            urlString =
                            try {
                                URL(redirectedUrl[0]).toString()
                            } catch (e: MalformedURLException){
                                // Maybe its a relative url. Use the original for context.
                                URL(request.url, redirectedUrl[0]).toString()
                            }
                        }
                        manager.request(encoding).response().second
                    } else {
                        //error
                        val error = FuelError().apply {
                            exception = RedirectException()
                            errorData = response.data
                            this.response = response
                        }
                        throw error
                    }
                } else {
                    next(request, response)
                }
            }
        }


