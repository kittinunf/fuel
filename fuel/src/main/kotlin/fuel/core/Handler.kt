package fuel.core

/**
 * Created by Kittinun Vantasin on 6/18/15.
 */

interface Handler<T> {

    fun success(request: Request, response: Response, t: T);
    fun failure(request: Request, response: Response, error: FuelError);

}
