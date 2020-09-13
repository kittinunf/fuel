@file:Suppress("unused")

package fuel

import okhttp3.RequestBody
import okhttp3.Response

public suspend inline fun String.httpGet(): Response = Fuel.get(this)
public suspend inline fun String.httpPost(requestBody: RequestBody): Response = Fuel.post(this, requestBody)
public suspend inline fun String.httpPut(requestBody: RequestBody): Response = Fuel.put(this, requestBody)
public suspend inline fun String.httpPatch(requestBody: RequestBody): Response = Fuel.patch(this, requestBody)
public suspend inline fun String.httpDelete(requestBody: RequestBody?): Response = Fuel.delete(this, requestBody)
public suspend inline fun String.httpHead(): Response = Fuel.head(this)
public suspend inline fun String.httpMethod(method: String, requestBody: RequestBody?): Response =
    Fuel.method(this, method, requestBody)
