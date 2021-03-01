// Inspired by https://github.com/coil-kt/coil/blob/master/coil-default/src/main/java/coil/api/Coils.kt
@file:Suppress("unused")

package fuel

import okhttp3.HttpUrl
import okhttp3.RequestBody
import okhttp3.Response

public suspend inline fun Fuel.get(
    uri: String
): Response = suspendLoader().get(Request.Builder().data(uri).build())

public suspend inline fun Fuel.get(
    url: HttpUrl
): Response = suspendLoader().get(Request.Builder().data(url).build())

public suspend inline fun Fuel.post(
    uri: String,
    requestBody: RequestBody
): Response = suspendLoader().post(Request.Builder().data(uri).requestBody(requestBody).build())

public suspend inline fun Fuel.post(
    url: HttpUrl,
    requestBody: RequestBody
): Response = suspendLoader().post(Request.Builder().data(url).requestBody(requestBody).build())

public suspend inline fun Fuel.put(
    uri: String,
    requestBody: RequestBody
): Response = suspendLoader().put(Request.Builder().data(uri).requestBody(requestBody).build())

public suspend inline fun Fuel.put(
    url: HttpUrl,
    requestBody: RequestBody
): Response = suspendLoader().put(Request.Builder().data(url).requestBody(requestBody).build())

public suspend inline fun Fuel.patch(
    url: HttpUrl,
    requestBody: RequestBody
): Response = suspendLoader().patch(Request.Builder().data(url).requestBody(requestBody).build())

public suspend inline fun Fuel.patch(
    uri: String,
    requestBody: RequestBody
): Response = suspendLoader().patch(Request.Builder().data(uri).requestBody(requestBody).build())

public suspend inline fun Fuel.delete(
    url: HttpUrl,
    requestBody: RequestBody?
): Response = suspendLoader().delete(Request.Builder().data(url).requestBody(requestBody).build())

public suspend inline fun Fuel.delete(
    uri: String,
    requestBody: RequestBody?
): Response = suspendLoader().delete(Request.Builder().data(uri).requestBody(requestBody).build())

public suspend inline fun Fuel.head(
    url: HttpUrl
): Response = suspendLoader().head(Request.Builder().data(url).build())

public suspend inline fun Fuel.head(
    uri: String
): Response = suspendLoader().head(Request.Builder().data(uri).build())

public suspend inline fun Fuel.method(
    url: HttpUrl,
    method: String?,
    requestBody: RequestBody?
): Response = suspendLoader().method(Request.Builder().data(url).method(method).requestBody(requestBody).build())

public suspend inline fun Fuel.method(
    uri: String,
    method: String?,
    requestBody: RequestBody?
): Response = suspendLoader().method(Request.Builder().data(uri).method(method).requestBody(requestBody).build())

public suspend inline fun Fuel.request(convertible: RequestConvertible): Response = suspendLoader().method(convertible.request)
