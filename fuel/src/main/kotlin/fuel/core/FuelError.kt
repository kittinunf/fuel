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
        val elements = arrayListOf(response.toString())
        //exception
        elements.add("Exception : ${exception.getMessage()}")
        return elements.join("\n").toString()
    }

}
