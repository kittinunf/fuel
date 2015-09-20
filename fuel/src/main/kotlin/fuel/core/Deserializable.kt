package fuel.core

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader

/**
 * Created by Kittinun Vantasin on 8/16/15.
 */

internal interface Deserializable<out T: Any> {

    fun deserialize(response: Response): T

}

public interface ResponseDeserializable<out T : Any> : Deserializable<T> {

    override fun deserialize(response: Response): T {
        return deserialize(response.data) ?:
                deserialize(ByteArrayInputStream(response.data)) ?:
                deserialize(InputStreamReader(ByteArrayInputStream(response.data))) ?:
                deserialize(String(response.data)) ?:
                throw IllegalStateException("One of deserialize(ByteArray) or deserialize(InputStream) or deserialize(Reader) or deserialize(String) must be implemented")
    }

    //One of these methods must be implemented
    public fun deserialize(bytes: ByteArray): T? = null

    public fun deserialize(inputStream: InputStream): T? = null

    public fun deserialize(reader: Reader): T? = null

    public fun deserialize(content: String): T? = null

}

internal fun <T: Any, U : Deserializable<T>> Request.response(deserializable: U, handler: (Request, Response, Either<FuelError, T>) -> Unit) {
    response(deserializable, { request, response, value ->
        handler(this@response, response, Right(value))
    }, { request, response, error ->
        handler(this@response, response, Left(error))
    })
}

internal fun <T: Any, U : Deserializable<T>> Request.response(deserializable: U, handler: Handler<T>) {
    response(deserializable, { request, response, value ->
        handler.success(request, response, value)
    }, { request, response, error ->
        handler.failure(request, response, error)
    })
}

internal fun <T: Any, U : Deserializable<T>> Request.response(deserializable: U,
                                                        success: (Request, Response, T) -> Unit,
                                                        failure: (Request, Response, FuelError) -> Unit) {
    taskRequest.apply {
        successCallback = { response ->

            val deliverable: Either<Exception, T> =
                    try {
                        Right(deserializable.deserialize(response))
                    } catch(exception: Exception) {
                        Left(exception)
                    }

            callback {
                deliverable.fold({
                    val error = FuelError().apply {
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
