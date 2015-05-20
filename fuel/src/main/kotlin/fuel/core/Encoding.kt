package fuel.core

import fuel.Fuel
import fuel.util.build
import java.net.URLEncoder
import kotlin.properties.Delegates

/**
 * Created by Kittinun Vantasin on 5/19/15.
 */

public class Encoding : Fuel.RequestConvertible {

    val ENCODING = "UTF-8"

    var httpMethod: Method by Delegates.notNull()
    var urlString: String by Delegates.notNull()
    var parameters: Map<String, Any?>? = null

    var encoder: (Method, String, Map<String, Any?>?) -> Request = { method, path, parameters ->
        val request = Request()
        var modifiedPath = path
        var data: ByteArray? = null
        var headerPair: Pair<String, String>? = null

        if (encodeParameterInUrl(method)) {
            val query = if (path.last().equals("?")) "" else "?"
            modifiedPath += query + queryFromParameters(parameters)
        } else {
            headerPair = ("Content-Type" to "application/x-www-form-urlencoded")
            data = queryFromParameters(parameters).toByteArray()
        }

        build(request) {
            httpMethod = method
            this.path = modifiedPath
            this.httpBody = data
            header(headerPair)
        }
    }

    override val request by Delegates.lazy { encoder(httpMethod, urlString, parameters) }

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
