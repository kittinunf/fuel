package fuel.core

import kotlin.properties.Delegates

/**
 * Created by Kittinun Vantasin on 5/18/15.
 */

public class FuelError : Exception() {

    var exception: Exception by Delegates.notNull()
    var response: Response by Delegates.notNull()

    //data
    var errorData = ByteArray(0)

    override fun toString(): String {
        return StringBuilder {
            append(response.toString())
            append("Exception : ${exception.getMessage()}\n")
        }.toString()
    }
}
