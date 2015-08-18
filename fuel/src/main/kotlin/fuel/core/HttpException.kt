package fuel.core

/**
 * Created by Kittinun Vantasin on 8/17/15.
 */

public class HttpException(val httpCode: Int, val httpMessage: String) : Exception("HTTP Exception $httpCode $httpMessage") {

    override fun toString(): String {
        return "HTTP Exception $httpCode $httpMessage"
    }

}