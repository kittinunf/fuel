package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.core.requests.DefaultRequest
import com.ibm.icu.text.IDNA
import java.net.MalformedURLException
import java.net.URI
import java.net.URL

class Encoding(
    val httpMethod: Method,
    val urlString: String,
    val baseUrlString: String? = null,
    val parameters: Parameters? = null
) : RequestFactory.RequestConvertible {

    private val idna: IDNA = IDNA.getUTS46Instance(flags)

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

        val uri = URI(
            url.protocol,
            url.userInfo,
            // converts domain to A-Label (RFC 5891)
            domainToAscii(url.host),
            url.port,
            url.path,
            url.query,
            url.ref
        )

        return URL(uri.toASCIIString())
    }

    private fun domainToAscii(domain: String): String {
        val info = IDNA.Info()
        val sb = StringBuilder()
        val domainAscii = idna.nameToASCII(domain, sb, info).toString()
        if (info.hasErrors()) {
            throw MalformedURLException(info.errors.toString())
        }
        return domainAscii
    }

    private val defaultHeaders = Headers.from()

    companion object {
        private const val flags =
            IDNA.CHECK_BIDI or IDNA.CHECK_CONTEXTJ or IDNA.CHECK_CONTEXTO or IDNA.NONTRANSITIONAL_TO_ASCII or IDNA.USE_STD3_RULES
    }
}
