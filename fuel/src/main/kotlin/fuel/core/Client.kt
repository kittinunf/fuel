package fuel.core

/**
 * Created by Kittinun Vantasin on 5/14/15.
 */

interface Client {

    fun executeRequest(request: Request): Response

}