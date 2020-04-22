package fuel

import okhttp3.RequestBody

suspend inline fun String.httpGet() = Fuel.get(this)
suspend inline fun String.httpPost(requestBody: RequestBody) = Fuel.post(this, requestBody)
suspend inline fun String.httpPut(requestBody: RequestBody) = Fuel.put(this, requestBody)
suspend inline fun String.httpPatch(requestBody: RequestBody) = Fuel.patch(this, requestBody)
suspend inline fun String.httpDelete(requestBody: RequestBody?) = Fuel.delete(this, requestBody)
suspend inline fun String.httpHead() = Fuel.head(this)
suspend inline fun String.httpMethod(method: String, requestBody: RequestBody?) =
        Fuel.method(this, method, requestBody)
