package fuel.core

import fuel.Fuel
import fuel.util.build
import fuel.util.toHexString
import java.net.URL
import java.net.URLEncoder
import kotlin.properties.Delegates

/**
 * Created by Kittinun Vantasin on 5/19/15.
 */

public class Encoding : Fuel.RequestConvertible {

    val ENCODING = "UTF-8"

    var requestType: Request.Type = Request.Type.REQUEST
    var httpMethod: Method by Delegates.notNull()
    var baseUrlString: String? = null
    var urlString: String by Delegates.notNull()
    var parameters: Map<String, Any?>? = null

    var encoder: (Method, String, Map<String, Any?>?) -> Request = { method, path, parameters ->

        var modifiedPath = path
        var data: ByteArray? = null
        var headerPairs: MutableMap<String, Any> = hashMapOf("Accept-Encoding" to "compress;q=0.5, gzip;q=1.0")

        if (encodeParameterInUrl(method)) {
            val query = if (path.last().equals("?")) "" else "?"
            modifiedPath += query + queryFromParameters(parameters)
        } else if (requestType.equals(Request.Type.UPLOAD)) {
            val boundary = System.currentTimeMillis().toHexString()
            headerPairs.plusAssign("Content-Type" to "multipart/form-data; boundary=" + boundary)
        } else {
            headerPairs.plusAssign("Content-Type" to "application/x-www-form-urlencoded")
            data = queryFromParameters(parameters).toByteArray()
        }

        build(Request()) {
            httpMethod = method
            this.path = modifiedPath
            this.url = createUrl(modifiedPath)
            this.httpBody = data
            this.type = requestType
            header(headerPairs)
        }

    }

    override val request by Delegates.lazy { encoder(httpMethod, urlString, parameters) }

    private fun createUrl(path: String): URL {
        if (baseUrlString != null) {
            return URL(baseUrlString + if (!path.startsWith('/')) '/' + path else path)
        } else {
            return URL(path)
        }
    }

    private fun encodeParameterInUrl(method: Method): Boolean {
        when(method) {
            Method.GET, Method.DELETE -> return true
            else -> return false
        }
    }

    private fun queryFromParameters(params: Map<String, Any?>?): String {
        if (params == null) return ""

        val list = arrayListOf<String>()
        for ((key, value) in parameters) {
            if (value != null) {
                list.add("${URLEncoder.encode(key, ENCODING)}=${URLEncoder.encode(value.toString(), ENCODING)}")
            }
        }
        return list.join("&")
    }

}
