package fuel.core

import org.apache.commons.io.IOUtils
import java.io.InputStream
import kotlin.properties.Delegates

/**
 * Created by Kittinun Vantasin on 5/13/15.
 */

public class Response {

    var httpStatusCode: Int = -1

    var dataStream: InputStream by Delegates.notNull()

    val data: ByteArray by Delegates.lazy {
        IOUtils.toByteArray(dataStream)
    }

}

public inline fun Response(builder: Response.() -> Unit): Response {
    val response = Response()
    response.builder()
    return response
}
