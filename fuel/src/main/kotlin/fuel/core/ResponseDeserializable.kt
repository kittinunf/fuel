package fuel.core

import fuel.util.build

/**
 * Created by Kittinun Vantasin on 8/16/15.
 */

public interface ResponseDeserializable<T> {

    val deserializer: (Request, Response) -> Either<FuelError, T>

}

public class GenericResponseDeserializer<T>(override val deserializer: (Request, Response) -> Either<FuelError, T>) : ResponseDeserializable<T>

private fun <T, U : ResponseDeserializable<T>> Request.response(deserializable: U, handler: (Request, Response, Either<FuelError, T>) -> Unit) {
    build(taskRequest) {
        successCallback = { response ->

            val deliverable = deserializable.deserializer.invoke(this@response, response)

            call {
                handler(this@response, response, deliverable)
            }
        }

        failureCallback = { error, response ->
            call {
                handler(this@response, response, Left(error))
            }
        }
    }

    submit(taskRequest)
}

private fun <T, U: ResponseDeserializable<T>> Request.response(deserializable: U, handler: Handler<T>) {
    build(taskRequest) {
        successCallback = { response ->

            val deliverable = deserializable.deserializer.invoke(this@response, response)

            call {
                when (deliverable) {
                    is Left -> handler.failure(this@response, response, deliverable.left)
                    is Right -> handler.success(this@response, response, deliverable.right)
                }
            }

        }

        failureCallback = { error, response ->
            call {
                handler.failure(this@response, response, error)
            }
        }
    }

    submit(taskRequest)
}

//byte array
public fun Request.response(handler: (Request, Response, Either<FuelError, ByteArray>) -> Unit): Unit = response(Request.byteArrayDeserializer(), handler)
public fun Request.response(handler: Handler<ByteArray>): Unit = response(Request.byteArrayDeserializer(), handler)

//string
public fun Request.responseString(handler: (Request, Response, Either<FuelError, String>) -> Unit): Unit = response(Request.stringDeserializer(), handler)
public fun Request.responseString(handler: Handler<String>): Unit = response(Request.stringDeserializer(), handler)
