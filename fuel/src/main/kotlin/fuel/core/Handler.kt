package fuel.core

/**
 * Created by Kittinun Vantasin on 6/18/15.
 */

interface Handler<T> {

    fun handle(request: Request, response: Response, either: Either<FuelError, T>);

}
