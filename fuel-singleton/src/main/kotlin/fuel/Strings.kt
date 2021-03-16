@file:Suppress("unused")

package fuel

import okhttp3.Call
import okhttp3.RequestBody

public fun String.httpGet(): Call = Fuel.get(this)

public fun String.httpPost(requestBody: RequestBody): Call = Fuel.post(this, requestBody)

public fun String.httpPut(requestBody: RequestBody): Call = Fuel.put(this, requestBody)

public fun String.httpPatch(requestBody: RequestBody): Call = Fuel.patch(this, requestBody)

public fun String.httpDelete(requestBody: RequestBody?): Call = Fuel.delete(this, requestBody)

public fun String.httpHead(): Call = Fuel.head(this)

public fun String.httpMethod(method: String, requestBody: RequestBody?): Call = Fuel.method(this, method, requestBody)
