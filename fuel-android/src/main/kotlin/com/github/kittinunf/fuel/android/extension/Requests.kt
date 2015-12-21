package com.github.kittinunf.fuel.android.extension

import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.result.Result
import org.json.JSONObject

/**
 * Created by Kittinun Vantasin on 11/9/15.
 */

//jsonObject
public fun Request.responseJson(handler: (Request, Response, Result<JSONObject, FuelError>) -> Unit) =
        response(jsonDeserializer(), handler)

public fun Request.responseJson(handler: Handler<JSONObject>) = response(jsonDeserializer(), handler)

public fun jsonDeserializer(): Deserializable<JSONObject> {
    return object : Deserializable<JSONObject> {
        override fun deserialize(response: Response): JSONObject {
            return JSONObject(String(response.data))
        }
    }
}
