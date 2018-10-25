package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.Fuel
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.net.URLEncoder

class Encoding(
    val httpMethod: Method,
    val urlString: String,
    val requestType: Request.Type = Request.Type.REQUEST,
    val baseUrlString: String? = null,
    val parameters: List<Pair<String, Any?>>? = null,
    val timeoutInMillisecond: Int = 15000,
    val timeoutReadInMillisecond: Int = timeoutInMillisecond
) : Fuel.RequestConvertible {

    private val encoder: (Method, String, List<Pair<String, Any?>>?) -> Request = { method, path, parameters ->
        var modifiedPath = path
        var data: String? = null
        val headerPairs = Headers.from(defaultHeaders)
        when {
            encodeParameterInUrl(method) -> {
                var querySign = ""
                val queryParamString = queryFromParameters(parameters)
                if (queryParamString.isNotEmpty()) {
                    if (path.count() > 0) {
                        querySign = if (path.last() == '?') "" else "?"
                    }
                }
                modifiedPath += (querySign + queryParamString)
            }
            requestType == Request.Type.UPLOAD -> {
                val boundary = System.currentTimeMillis().toString(16)
                headerPairs[Headers.CONTENT_TYPE] = "multipart/form-data; boundary=$boundary"
            }
            else -> {
                headerPairs[Headers.CONTENT_TYPE] = "application/x-www-form-urlencoded"
                data = queryFromParameters(parameters)
            }
        }
        Request(
                method = method,
                path = modifiedPath,
                url = createUrl(modifiedPath),
                type = requestType,
                parameters = parameters ?: emptyList(),
                timeoutInMillisecond = timeoutInMillisecond,
                timeoutReadInMillisecond = timeoutReadInMillisecond,
                headers = headerPairs
        ).apply {
            if (data != null) body(data)
        }
    }

    override val request by lazy { encoder(httpMethod, urlString, parameters) }

    private fun createUrl(path: String): URL {
        val url = try {
            // give precedence to local path
            URL(path)
        } catch (e: MalformedURLException) {
            var base = baseUrlString ?: ""
            if (base.endsWith('/')) {
                // remove last slash
                base = base.substring(0, base.count() - 1)
            }
            URL(base + if (path.startsWith('/') or path.isEmpty()) path else "/$path")
        }
        val uri = try {
            url.toURI()
        } catch (e: URISyntaxException) {
            URI(url.protocol, url.userInfo, url.host, url.port, url.path, url.query, url.ref)
        }
        return URL(uri.toASCIIString())
    }

    private fun encodeParameterInUrl(method: Method): Boolean = when (method) {
        Method.GET, Method.DELETE, Method.HEAD -> true
        else -> false
    }

    private fun queryFromParameters(params: List<Pair<String, Any?>>?): String = params.orEmpty()
            .filterNot { it.second == null }
            .map { (key, value) -> URLEncoder.encode(key, "UTF-8") to URLEncoder.encode("$value", "UTF-8") }
            .joinToString("&") { (key, value) -> "$key=$value" }

    private val defaultHeaders = Headers.from(mapOf(Headers.ACCEPT_ENCODING to "compress;q=0.5, gzip;q=1.0"))
}
