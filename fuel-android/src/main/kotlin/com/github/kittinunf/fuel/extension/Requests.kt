package com.github.kittinunf.fuel.extension

import com.github.kittinunf.fuel.core.Deserializable
import com.github.kittinunf.fuel.core.Either
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Handler
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.response
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
