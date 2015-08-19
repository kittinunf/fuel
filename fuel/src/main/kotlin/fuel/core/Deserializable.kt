package fuel.core

import fuel.util.build
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader

/**
 * Created by Kittinun Vantasin on 8/16/15.
 */

private interface Deserializable<T> {

    fun deserialize(response: Response): T?

}

public interface ResponseDeserializable<T> : Deserializable<T> {

    override fun deserialize(response: Response): T? {

        return deserialize(response.data) ?:
                deserialize(ByteArrayInputStream(response.data)) ?:
                deserialize(InputStreamReader(ByteArrayInputStream(response.data))) ?:
                deserialize(String(response.data))
    }

    public fun deserialize(bytes: ByteArray): T? = null
    public fun deserialize(inputStream: InputStream): T? = null
    public fun deserialize(reader: Reader): T? = null
    public fun deserialize(content: String): T? = null

}

private fun <T, U : Deserializable<T>> Request.response(deserializable: U, handler: (Request, Response, Either<FuelError, T>) -> Unit) {
    response(deserializable, { request, response, value ->
        handler(this@response, response, Right(value))
    }, { request, response, error ->
        handler(this@response, response, Left(error))
    })
}

private fun <T, U : Deserializable<T>> Request.response(deserializable: U, handler: Handler<T>) {
    response(deserializable, { request, response, value ->
        handler.success(request, response, value)
    }, { request, response, error ->
        handler.failure(request, response, error)
    })
}

private fun <T, U : Deserializable<T>> Request.response(deserializable: U,
                                                        success: (Request, Response, T) -> Unit,
                                                        failure: (Request, Response, FuelError) -> Unit) {
    build(taskRequest) {
        successCallback = { response ->

            val deliverable: Either<Exception, T> =
                    try {
                        val value = (deserializable.deserialize(response))
                        if (value == null) Left(Exception()) else Right(value)
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
