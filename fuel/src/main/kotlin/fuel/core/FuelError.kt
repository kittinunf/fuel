package fuel.core

import org.apache.commons.io.IOUtils
import java.io.InputStream
import kotlin.properties.Delegates

/**
 * Created by Kittinun Vantasin on 5/18/15.
 */

public class FuelError : Exception() {

    var exception: Exception by Delegates.notNull()
    var response: Response by Delegates.notNull()

    val errorStream: InputStream? by Delegates.lazy { response.dataStream }
    val error: ByteArray? by Delegates.lazy { response.data }

}
