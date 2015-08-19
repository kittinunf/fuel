package fuel.core

/**
 * Created by Kittinun Vantasin on 5/14/15.
 */

public interface Client {

    fun executeRequest(request: Request): Response

}