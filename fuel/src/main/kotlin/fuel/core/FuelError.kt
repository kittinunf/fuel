package fuel.core

import kotlin.properties.Delegates

/**
 * Created by Kittinun Vantasin on 5/18/15.
 */

public class FuelError : Exception() {

    var exception: Exception by Delegates.notNull()

    //data
    var errorData = ByteArray(0)

    override fun toString(): String {
        val elements = arrayListOf("Exception : ${exception.getMessage()}")
        //exception
        return elements.join("\n").toString()
    }

}
