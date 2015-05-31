package fuel.core

import fuel.util.readWriteLazy
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

    //data
    var data = ByteArray(0)

}
