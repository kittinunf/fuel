package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.Fuel
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL

class Encoding(val httpMethod: Method,
               val urlString: String,
               val requestType: Request.Type = Request.Type.REQUEST,
               val baseUrlString: String? = null,
               val parameters: List<Pair<String, Any?>>? = null) : Fuel.RequestConvertible {

    private val encoder: (Method, String, List<Pair<String, Any?>>?) -> Request = { method, path, parameters ->
        var modifiedPath = path
        var data: String? = null
        val headerPairs = defaultHeaders.toMutableMap()
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
                headerPairs += "Content-Type" to "multipart/form-data; boundary=" + boundary
            }
            else -> {
                headerPairs += "Content-Type" to "application/x-www-form-urlencoded"
                data = queryFromParameters(parameters)
            }
        }
        Request(
                method = method,
                path = modifiedPath,
                url = createUrl(modifiedPath),
                type = requestType,
                parameters = parameters ?: emptyList()
        ).apply {
            header(headerPairs, false)
            if (data != null) body(data!!)
        }

    }

    override val request by lazy { encoder(httpMethod, urlString, parameters) }

    private fun createUrl(path: String): URL {
        val url = try {
            //give precedence to local path
            URL(path)
        } catch (e: MalformedURLException) {
            URL(baseUrlString + if (path.startsWith('/') or path.isEmpty()) path else '/' + path)
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
            .joinToString("&") { "${it.first}=${it.second}" }

    private val defaultHeaders = mapOf("Accept-Encoding" to "compress;q=0.5, gzip;q=1.0")
}
