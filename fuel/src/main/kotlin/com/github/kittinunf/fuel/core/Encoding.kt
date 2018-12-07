package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.core.requests.DefaultRequest
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL

class Encoding(
    val httpMethod: Method,
    val urlString: String,
    val baseUrlString: String? = null,
    val parameters: Parameters? = null
) : RequestFactory.RequestConvertible {

    private val encoder: (Method, String, Parameters?) -> Request = { method, path, parameters ->
        DefaultRequest(
            method = method,
            url = createUrl(path),
            parameters = parameters ?: emptyList(),
            headers = Headers.from(defaultHeaders)
        )
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

    private val defaultHeaders = Headers.from()
}
