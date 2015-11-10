package extension

import fuel.core.*
import org.json.JSONObject

/**
 * Created by Kittinun Vantasin on 11/9/15.
 */

//jsonObject
public fun Request.responseJson(handler: (Request, Response, Either<FuelError, JSONObject>) -> Unit): Unit =
        response(jsonDeserializer(), handler)

public fun Request.responseJson(handler: Handler<JSONObject>): Unit = response(jsonDeserializer(), handler)

public fun jsonDeserializer(): Deserializable<JSONObject> {
    return object : Deserializable<JSONObject> {
        override fun deserialize(response: Response): JSONObject {
            return JSONObject(String(response.data))
        }
    }
}
