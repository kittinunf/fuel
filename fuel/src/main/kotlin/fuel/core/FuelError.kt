package fuel.core

import java.io.InputStream
import kotlin.properties.Delegates

/**
 * Created by Kittinun Vantasin on 5/18/15.
 */

public class FuelError : Exception() {

    var exception: Exception by Delegates.notNull()
    var response: Response by Delegates.notNull()

    var errorDataStream: InputStream? = null
    val errorData: ByteArray by Delegates.lazy {
        if (errorDataStream != null) errorDataStream!!readBytes(defaultBufferSize) else ByteArray(0)
    }

}
