package fuel.core

import java.io.InputStream
import kotlin.properties.Delegates

/**
 * Created by Kittinun Vantasin on 5/13/15.
 */

public class Response {

    var httpStatusCode = -1
    var httpResponseMessage: String by Delegates.notNull()
    var httpResponseHeaders: Map<String, List<String>> by Delegates.notNull()
    var httpContentLength = 0L

    var dataStream: InputStream? = null
    val data: ByteArray by Delegates.lazy {
        if (dataStream != null) {
            val bytes = dataStream!!.readBytes(defaultBufferSize)
            dataStream!!.close()
            bytes
        } else {
            ByteArray(0)
        }
    }

}
