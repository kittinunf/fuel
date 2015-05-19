package fuel.core

import org.apache.commons.io.IOUtils
import java.io.InputStream
import kotlin.properties.Delegates

/**
 * Created by Kittinun Vantasin on 5/13/15.
 */

public class Response {

    var httpStatusCode: Int by Delegates.notNull()
    var httpResponseMessage: String by Delegates.notNull()

    var dataStream: InputStream? = null
    val data: ByteArray by Delegates.lazy {
        var bytes = ByteArray(0)
        if (dataStream != null) {
            bytes = IOUtils.toByteArray(dataStream)
        }
        bytes
    }

    override fun toString(): String {
        return "Response: { StatusCode: $httpStatusCode, ResponseMessage: $httpResponseMessage }"
    }
}
