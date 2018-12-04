package com.github.kittinunf.fuel.core.interceptors

import com.github.kittinunf.fuel.core.FoldableRequestInterceptor
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Parameters
import com.github.kittinunf.fuel.core.RequestTransformer
import java.net.URL
import java.net.URLEncoder

object ParameterEncoder : FoldableRequestInterceptor {
    override fun invoke(next: RequestTransformer): RequestTransformer {
        return inner@{ request ->
            val contentType = request[Headers.CONTENT_TYPE].lastOrNull()

            // Expect the parameters to be already encoded in the body
            if (contentType?.startsWith("multipart/form-data") == true) {
                return@inner next(request)
            }

            // If it can be added to the body
            if (request.body.isEmpty() && allowParametersInBody(request.method)) {
                if (contentType.isNullOrBlank() || contentType.startsWith("application/x-www-form-urlencoded")) {
                    return@inner next(
                        request
                            .header(Headers.CONTENT_TYPE, "application/x-www-form-urlencoded")
                            .body(encode(request.parameters))
                            .apply { parameters = emptyList() }
                    )
                }
            }

            // Has to be added to the URL
            next(
                request
                    .apply { url = url.withParameters(parameters) }
                    .apply { parameters = emptyList() }
            )
        }
    }

    private fun encode(parameters: Parameters) =
        parameters
            .filterNot { (_, values) -> values == null }
            .flatMap { (key, values) ->
                // Deal with arrays
                ((values as? Iterable<*>)?.toList() ?: (values as? Array<*>)?.toList())?.let {
                    val encodedKey = "${URLEncoder.encode(key, "UTF-8")}[]"
                    it.map { value -> encodedKey to URLEncoder.encode(value.toString(), "UTF-8") }

                    // Deal with regular
                } ?: listOf(URLEncoder.encode(key, "UTF-8") to URLEncoder.encode(values.toString(), "UTF-8"))
            }
            .joinToString("&") { (key, value) -> if (value.isBlank()) key else "$key=$value" }

    private fun allowParametersInBody(method: Method) = when (method) {
        Method.POST, Method.PATCH, Method.PUT -> true
        else -> false
    }


    private fun URL.withParameters(parameters: Parameters): URL {
        val encoded = ParameterEncoder.encode(parameters)
        if (encoded.isEmpty()) {
            return this
        }

        val joiner = if (toExternalForm().contains('?')) {
            // There is already some query
            if (query.isNotEmpty()) "&"
            // There is already a trailing ?
            else ""
        } else "?"

        return URL(toExternalForm() + joiner + encoded)
    }
}