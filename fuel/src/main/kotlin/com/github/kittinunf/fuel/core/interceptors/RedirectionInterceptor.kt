package com.github.kittinunf.fuel.core.interceptors

import com.github.kittinunf.fuel.core.Encoding
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.isStatusRedirection
import java.net.URI
import java.net.URL
import javax.net.ssl.HttpsURLConnection

private val redirectStatusWithGets = listOf(
    HttpsURLConnection.HTTP_MOVED_PERM,
    HttpsURLConnection.HTTP_MOVED_TEMP,
    HttpsURLConnection.HTTP_SEE_OTHER
)

fun redirectResponseInterceptor(manager: FuelManager) =
        { next: (Request, Response) -> Response ->
            inner@{ request: Request, response: Response ->
                if (!response.isStatusRedirection || !request.isAllowRedirects) {
                    return@inner next(request, response)
                }

                val redirectedUrl = response[Headers.LOCATION].ifEmpty { response[Headers.CONTENT_LOCATION] }.lastOrNull()
                if (redirectedUrl.isNullOrEmpty()) {
                    return@inner next(request, response)
                }

                val newUrl = if (URI(redirectedUrl).isAbsolute) {
                    URL(redirectedUrl)
                } else {
                    URL(request.url, redirectedUrl)
                }
                val newHeaders = Headers.from(request.headers)

                val newMethod = when {
                    response.statusCode in redirectStatusWithGets -> Method.GET
                    else -> request.method
                }

                val encoding = Encoding(httpMethod = newMethod, urlString = newUrl.toString())

                // check whether it is the same host or not
                if (newUrl.host != request.url.host) {
                    newHeaders.remove(Headers.AUTHORIZATION)
                }

                // redirect
                next(request, manager.request(encoding).header(newHeaders).response().second)
            }
        }
