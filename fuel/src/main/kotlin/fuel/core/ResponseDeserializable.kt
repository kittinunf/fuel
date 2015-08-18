package fuel.core

import fuel.util.build
import org.json.JSONObject

/**
 * Created by Kittinun Vantasin on 8/16/15.
 */

public interface ResponseDeserializable<T> {

    val deserializer: (Request, Response) -> Either<Exception, T>

}

private fun <T, U : ResponseDeserializable<T>> Request.response(deserializable: U,
                                                                success: (Request, Response, T) -> Unit,
                                                                failure: (Request, Response, FuelError) -> Unit) {
    build(taskRequest) {
        successCallback = { response ->
            val deliverable = deserializable.deserializer.invoke(this@response, response)

            callback {
                deliverable.fold({
                    val error = build(FuelError()) {
                        exception = deliverable.get()
                    }
                    failure(this@response, response, error)
                }, {
                    success(this@response, response, it)
                })
            }
        }

        failureCallback = { error, response ->
            callback {
                failure(this@response, response, error)
            }
        }
    }

    submit(taskRequest)
}

private fun <T, U : ResponseDeserializable<T>> Request.response(deserializable: U, handler: (Request, Response, Either<FuelError, T>) -> Unit) {
    response(deserializable, { request, response, value ->
        handler(this@response, response, Right(value))
    }, { request, response, error ->
        handler(this@response, response, Left(error))
    })
}

private fun <T, U : ResponseDeserializable<T>> Request.response(deserializable: U, handler: Handler<T>) {
    response(deserializable, { request, response, value ->
        handler.success(request, response, value)
    }, { request, response, error ->
        handler.failure(request, response, error)
    })
}

