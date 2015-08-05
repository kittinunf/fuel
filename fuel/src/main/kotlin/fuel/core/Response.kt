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
    var httpResponseHeaders = emptyMap<String, List<String>>()
    var httpContentLength = 0L

    //data
    var data = ByteArray(0)

    override fun toString(): String {
        val elements = arrayListOf("<-- $httpStatusCode (${url.toString()})")

        //response message
        elements.add("Response : $httpResponseMessage")

        //body
        elements.add("Body : ${ if (data.size() != 0) String(data) else "(empty)"}")

        //headers
        //headers
        elements.add("Headers : (${httpResponseHeaders.size()})")
        for ((key, value) in httpResponseHeaders) {
            elements.add("$key : $value")
        }

        return elements.join("\n").toString()
    }

}
