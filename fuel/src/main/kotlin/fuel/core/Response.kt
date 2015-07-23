package fuel.core

import java.net.URL
import kotlin.properties.Delegates

/**
 * Created by Kittinun Vantasin on 5/13/15.
 */

public class Response {

    var url: URL by Delegates.notNull()
    var httpStatusCode = -1
    var httpResponseMessage = ""
    var httpResponseHeaders: Map<String, List<String>> by Delegates.notNull()
    var httpContentLength = 0L

    //data
    var data = ByteArray(0)

    override fun toString(): String {
        return StringBuilder {
            append("<-- $httpStatusCode (${url.toString()})\n")
            append("Response : $httpResponseMessage\n")
            append("Body : ${ if (data.size() != 0) String(data) else "(empty)"}\n")

            append("Headers : (${httpResponseHeaders.size()})\n")
            for ((key, value) in httpResponseHeaders) {
                append("$key : $value\n")
            }
        }.toString()
    }
}
