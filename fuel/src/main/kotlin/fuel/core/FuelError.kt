package fuel.core

import fuel.util.readWriteLazy
import java.io.InputStream
import kotlin.properties.Delegates

/**
 * Created by Kittinun Vantasin on 5/18/15.
 */

public class FuelError : Exception() {

    var exception: Exception by Delegates.notNull()
    var response: Response by Delegates.notNull()

    //data
    var errorData = ByteArray(0)

}
