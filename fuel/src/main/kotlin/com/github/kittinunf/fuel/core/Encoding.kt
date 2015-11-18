package com.github.kittinunf.fuel.core

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.util.toHexString
import java.net.MalformedURLException
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import kotlin.properties.Delegates

/**
 * Created by Kittinun Vantasin on 5/19/15.
 */

public class Encoding : Fuel.RequestConvertible {

    var requestType: Request.Type = Request.Type.REQUEST
    var httpMethod: Method by Delegates.notNull()
    var baseUrlString: String? = null
    var urlString: String by Delegates.notNull()
    var parameters: List<Pair<String, Any?>>? = null

    var encoder: (Method, String, List<Pair<String, Any?>>?) -> Request = { method, path, parameters ->

        var modifiedPath = path
        var data: ByteArray? = null
        var headerPairs: MutableMap<String, Any> = hashMapOf("Accept-Encoding" to "compress;q=0.5, gzip;q=1.0")

        if (encodeParameterInUrl(method)) {
            var querySign = ""
            val queryParamString = queryFromParameters(parameters)
            if (queryParamString.isNotEmpty()) {
                if (path.count() > 0) {
                    querySign = if (path.last() == '?') "" else "?"
                }
            }
            modifiedPath += (querySign + queryParamString)
        } else if (requestType.equals(Request.Type.UPLOAD)) {
            val boundary = System.currentTimeMillis().toHexString()
            headerPairs.plusAssign("Content-Type" to "multipart/form-data; boundary=" + boundary)
        } else {
            headerPairs.plusAssign("Content-Type" to "application/x-www-form-urlencoded")
            data = queryFromParameters(parameters).toByteArray()
        }

        Request().apply {
            httpMethod = method
            this.path = modifiedPath
            this.url = createUrl(modifiedPath)
            this.httpBody = data ?: ByteArray(0)
            this.type = requestType
            header(headerPairs)
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
        val uri = URI(url.protocol, url.userInfo, url.host, url.port, url.path, url.query, url.ref)
        return uri.toURL()
    }

    private fun encodeParameterInUrl(method: Method): Boolean {
        when (method) {
            Method.GET, Method.DELETE -> return true
            else -> return false
        }
    }

    private fun queryFromParameters(params: List<Pair<String, Any?>>?): String {
        return params?.let {
            params.filterNot { it.second == null }
                    .mapTo(arrayListOf<String>()) { "${it.first}=${it.second}" }
                    .joinToString("&")
        } ?: ""
    }

}
