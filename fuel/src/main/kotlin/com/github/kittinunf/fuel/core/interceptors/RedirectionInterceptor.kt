package com.github.kittinunf.fuel.core.interceptors

import com.github.kittinunf.fuel.core.*
import java.net.MalformedURLException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class RedirectException : Exception("Redirection fail, not found URL to redirect to")
val HTTP_3XX_RANGE = 300..399

fun redirectResponseInterceptor(manager: FuelManager) =
        { next: (Request, Response) -> Response ->
            { request: Request, response: Response ->
                if (HTTP_3XX_RANGE.contains(response.httpStatusCode)) { // Check if response is 3xx HTTP Status Code
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


