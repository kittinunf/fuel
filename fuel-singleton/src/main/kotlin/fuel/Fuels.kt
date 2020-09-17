// Inspired by https://github.com/coil-kt/coil/blob/master/coil-default/src/main/java/coil/api/Coils.kt
@file:Suppress("unused")

package fuel

import okhttp3.HttpUrl
import okhttp3.RequestBody
import okhttp3.Response

public suspend inline fun Fuel.get(
    uri: String
): Response = httpLoader().get(Request.Builder().data(uri).build())

public suspend inline fun Fuel.get(
    url: HttpUrl
): Response = httpLoader().get(Request.Builder().data(url).build())

public suspend inline fun Fuel.post(
    uri: String,
    requestBody: RequestBody
): Response = httpLoader().post(Request.Builder().data(uri).requestBody(requestBody).build())

public suspend inline fun Fuel.post(
    url: HttpUrl,
    requestBody: RequestBody
): Response = httpLoader().post(Request.Builder().data(url).requestBody(requestBody).build())

public suspend inline fun Fuel.put(
    uri: String,
    requestBody: RequestBody
): Response = httpLoader().put(Request.Builder().data(uri).requestBody(requestBody).build())

public suspend inline fun Fuel.put(
    url: HttpUrl,
    requestBody: RequestBody
): Response = httpLoader().put(Request.Builder().data(url).requestBody(requestBody).build())

public suspend inline fun Fuel.patch(
    url: HttpUrl,
    requestBody: RequestBody
): Response = httpLoader().patch(Request.Builder().data(url).requestBody(requestBody).build())

public suspend inline fun Fuel.patch(
    uri: String,
    requestBody: RequestBody
): Response = httpLoader().patch(Request.Builder().data(uri).requestBody(requestBody).build())

public suspend inline fun Fuel.delete(
    url: HttpUrl,
    requestBody: RequestBody?
): Response = httpLoader().delete(Request.Builder().data(url).requestBody(requestBody).build())

public suspend inline fun Fuel.delete(
    uri: String,
    requestBody: RequestBody?
): Response = httpLoader().delete(Request.Builder().data(uri).requestBody(requestBody).build())

public suspend inline fun Fuel.head(
    url: HttpUrl
): Response = httpLoader().head(Request.Builder().data(url).build())

public suspend inline fun Fuel.head(
    uri: String
): Response = httpLoader().head(Request.Builder().data(uri).build())

public suspend inline fun Fuel.method(
    url: HttpUrl,
    method: String?,
    requestBody: RequestBody?
): Response = httpLoader().method(Request.Builder().data(url).method(method).requestBody(requestBody).build())

public suspend inline fun Fuel.method(
    uri: String,
    method: String?,
    requestBody: RequestBody?
): Response = httpLoader().method(Request.Builder().data(uri).method(method).requestBody(requestBody).build())

public suspend inline fun Fuel.request(convertible: RequestConvertible): Response =
    httpLoader().method(convertible.request)
