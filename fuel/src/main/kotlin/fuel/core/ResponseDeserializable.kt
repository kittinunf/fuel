package fuel.core

import fuel.util.build
import org.json.JSONObject

/**
 * Created by Kittinun Vantasin on 8/16/15.
 */

public interface ResponseDeserializable<T> {

    val deserializer: (Request, Response) -> T

}

private fun <T, U : ResponseDeserializable<T>> Request.response(deserializable: U,
                                                                success: (Request, Response, T) -> Unit,
                                                                failure: (Request, Response, FuelError) -> Unit) {
    build(taskRequest) {
        successCallback = { response ->

            val deliverable: Either<Exception, T> =
                    try {
                        Right(deserializable.deserializer(this@response, response))
                    } catch(exception: Exception) {
                        Left(exception)
                    }

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


